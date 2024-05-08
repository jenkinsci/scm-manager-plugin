package com.cloudogu.scmmanager.scm.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public abstract class ApiClient {

  private final ObjectMapper objectMapper;

  private final String protocol;

  protected ApiClient(String protocol) {
    this.protocol = protocol;
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
  }

  public String getProtocol() {
    return protocol;
  }

  public abstract <T> CompletableFuture<T> get(String url, String contentType, Class<T> type);

  public abstract String getBaseUrl();

  protected <T> CompletableFuture<T> execute(OkHttpClient client, Request.Builder requestBuilder, Class<T> type) {
    CompletableFuture<T> future = new CompletableFuture<>();
    client.newCall(requestBuilder.build()).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException ex) {
        future.completeExceptionally(ex);
      }

      @Override
      public void onResponse(Call call, Response response) {
        try (response) {
          if (response.code() == 200) {
            try (ResponseBody body = response.body()) {
              if (body == null) {
                future.complete(null);
              } else {
                T t = objectMapper.readValue(body.bytes(), type);
                future.complete(t);
              }
            } catch (Exception ex) {
              future.completeExceptionally(ex);
            }
          } else {
            future.completeExceptionally(new IllegalReturnStatusException(response.code()));
          }
        }
      }
    });

    return future;
  }
}
