package com.cloudogu.scmmanager.scm.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import java.util.concurrent.CompletableFuture;

public abstract class ApiClient {

  private final ObjectMapper objectMapper;

  private final String protocol;

  protected ApiClient(String protocol) {
    this.protocol = protocol;
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public String getProtocol() {
    return protocol;
  }

  public abstract <T> CompletableFuture<T> get(String url, String contentType, Class<T> type);

  protected <T> CompletableFuture<T> execute(AsyncHttpClient.BoundRequestBuilder requestBuilder, Class<T> type) {
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
