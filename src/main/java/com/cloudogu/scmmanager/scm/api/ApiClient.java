package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.HttpAuthentication;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import jenkins.plugins.asynchttpclient.AHC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public final class ApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(ApiClient.class);

  private final AsyncHttpClient client;
  private final HttpAuthentication authentication;
  private final ObjectMapper objectMapper;
  private final Function<String, String> urlModifier;

  public ApiClient(String serverUrl) {
    this(serverUrl, rb -> {
    });
  }

  public ApiClient(String serverUrl, HttpAuthentication authentication) {
    this(AHC.instance(), serverUrl, authentication);
  }

  public ApiClient(AsyncHttpClient client, String serverUrl, HttpAuthentication authentication) {
    this(client, authentication, fixServerUrl(serverUrl));
  }

  @VisibleForTesting
  static Function<String, String> fixServerUrl(String serverUrl) {
    String trimmedServerUrl = serverUrl.trim();
    String fixedServerUrl;
    if (trimmedServerUrl.endsWith("/")) {
      fixedServerUrl = trimmedServerUrl.substring(0, trimmedServerUrl.length() - 1);
    } else {
      fixedServerUrl = trimmedServerUrl;
    }
    return url -> {
      if (url.contains("://")) {
        return url;
      }
      return fixedServerUrl + url;
    };
  }

  @VisibleForTesting
  ApiClient(AsyncHttpClient client, HttpAuthentication authentication, Function<String, String> urlModifier) {
    this.client = client;
    this.authentication = authentication;
    this.objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    this.urlModifier = urlModifier;
  }

  public <T> Promise<T> get(String url, String contentType, Class<T> type) {
    LOG.info("get {} from {}", type.getName(), url);
    AsyncHttpClient.BoundRequestBuilder requestBuilder = client.prepareGet(urlModifier.apply(url));
    authentication.authenticate(requestBuilder);
    requestBuilder.addHeader("Accept", contentType);

    CompletableFuture<T> future = new CompletableFuture<>();
    requestBuilder.execute(new AsyncCompletionHandler<Response>() {
      @Override
      public void onThrowable(Throwable ex) {
        future.completeExceptionally(ex);
      }

      @Override
      public Response onCompleted(Response response) {
        if (response.getStatusCode() == 200) {
          try {
            T t = objectMapper.readValue(response.getResponseBodyAsBytes(), type);
            future.complete(t);
          } catch (Exception ex) {
            future.completeExceptionally(ex);
          }
        } else {
          // TODO more explicit exception
          future.completeExceptionally(new IllegalReturnStatusException(response.getStatusCode()));
        }
        return response;
      }
    });

    return new Promise<>(future);
  }

  private static class IllegalReturnStatusException extends Exception {
    private final int statusCode;

    private IllegalReturnStatusException(int statusCode) {
      this.statusCode = statusCode;
    }
  }

  public static class Promise<T> {

    private final CompletableFuture<T> future;

    public Promise(T value) {
      this.future = CompletableFuture.completedFuture(value);
    }

    public Promise(CompletableFuture<T> future) {
      this.future = future;
    }

    public <T2> Promise<T2> then(Function<T, T2> function) {
      return new Promise<>(future.thenApply(function));
    }

    public T mapError(Function<ApiError, T> errorConsumer) throws InterruptedException {
      try {
        return future.get();
      } catch (InterruptedException e) {
        throw e;
      } catch (ExecutionException e) {
        Throwable cause = e.getCause();
        ApiError error;
        String exceptionMessage = e.getMessage();
        if (cause instanceof JsonParseException || cause instanceof JsonMappingException) {
          LOG.debug("could not parse response", e);
          error = new ApiError("could not parse response: " + exceptionMessage.substring(0, exceptionMessage.indexOf('\n')));
        } else if (cause instanceof IllegalReturnStatusException) {
          error = new ApiError(((IllegalReturnStatusException) cause).statusCode);
        } else {
          error = new ApiError("unknown exception: " + exceptionMessage);
        }
        return errorConsumer.apply(error);
      }
    }

    public T orElseThrow(Function<ApiError, RuntimeException> exceptionProvider) throws InterruptedException {
      return mapError(error -> {
        throw exceptionProvider.apply(error);
      });
    }
  }

  public static class ApiError {
    private final int status;
    private final String message;

    public ApiError(String message) {
      status = 200;
      this.message = message;
    }

    public ApiError(int httpStatus) {
      this.status = httpStatus;
      message = "illegal http status code: " + httpStatus;
    }

    public int getStatus() {
      return status;
    }

    public String getMessage() {
      return message;
    }
  }
}
