package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.HttpAuthentication;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import jenkins.plugins.asynchttpclient.AHC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class ApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(ApiClient.class);

  private final AsyncHttpClient client;
  private final HttpAuthentication authentication;
  private final ObjectMapper objectMapper;
  private final Function<String, String> urlModifier;

  public ApiClient(String serverUrl) {
    this(serverUrl, rb -> {});
  }

  public ApiClient(String serverUrl, HttpAuthentication authentication) {
    this(AHC.instance(), serverUrl, authentication);
  }

  public ApiClient(AsyncHttpClient client, String serverUrl, HttpAuthentication authentication) {
    this(client, authentication, url -> {
      // TODO needed?
      //      if (url.contains("://")) {
      //        return url;
      //      }
      // TODO check slash suffix and prefix
      return serverUrl + url;
    });
  }

  @VisibleForTesting
  ApiClient(AsyncHttpClient client, HttpAuthentication authentication, Function<String, String> urlModifier) {
    this.client = client;
    this.authentication = authentication;
    this.objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    this.urlModifier = urlModifier;
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
          // TODO more explicit exception
          future.completeExceptionally(
            new IllegalStateException("server returned status code " + response.getStatusCode())
          );
        }
        return response;
      }
    });

    return future;
  }
}
