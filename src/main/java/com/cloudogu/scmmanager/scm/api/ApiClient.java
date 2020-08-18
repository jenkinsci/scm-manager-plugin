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
import java.util.concurrent.TimeoutException;
import java.util.function.UnaryOperator;

public final class ApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(ApiClient.class);

  private final AsyncHttpClient client;
  private final HttpAuthentication authentication;
  private final ObjectMapper objectMapper;
  private final UnaryOperator<String> urlModifier;

  public ApiClient(String serverUrl, HttpAuthentication authentication) {
    this(AHC.instance(), serverUrl, authentication);
  }

  public ApiClient(AsyncHttpClient client, String serverUrl, HttpAuthentication authentication) {
    this(client, authentication, fixServerUrl(serverUrl));
  }

  @VisibleForTesting
  static UnaryOperator<String> fixServerUrl(String serverUrl) {
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
  ApiClient(AsyncHttpClient client, HttpAuthentication authentication, UnaryOperator<String> urlModifier) {
    this.client = client;
    this.authentication = authentication;
    this.objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    this.urlModifier = urlModifier;
  }

  public static void handleException(ExecutionException e) {
    Throwable cause = e.getCause();
    if (cause instanceof JsonParseException || cause instanceof JsonMappingException) {
      LOG.warn("could not parse response for request", e);
    } else if (cause instanceof IllegalReturnStatusException) {
      LOG.warn("got error in request: {}", e.getMessage());
    } else if (cause instanceof TimeoutException) {
      LOG.warn("request timed out: {}", e.getMessage());
    } else {
      LOG.warn("got unknown exception in request", e);
    }
  }

  public <T> CompletableFuture<T> get(String url, String contentType, Class<T> type) {
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
          future.completeExceptionally(new IllegalReturnStatusException(response.getStatusCode()));
        }
        return response;
      }
    });

    return future;
  }

}
