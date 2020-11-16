package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.HttpAuthentication;
import com.google.common.annotations.VisibleForTesting;
import com.ning.http.client.AsyncHttpClient;
import jenkins.plugins.asynchttpclient.AHC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

public class HttpApiClient extends ApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(HttpApiClient.class);

  private final AsyncHttpClient client;
  private final HttpAuthentication authentication;
  private final UnaryOperator<String> urlModifier;

  public HttpApiClient(String serverUrl, HttpAuthentication authentication) {
    this(AHC.instance(), serverUrl, authentication);
  }

  public HttpApiClient(AsyncHttpClient client, String serverUrl, HttpAuthentication authentication) {
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
  HttpApiClient(AsyncHttpClient client, HttpAuthentication authentication, UnaryOperator<String> urlModifier) {
    super("http");
    this.client = client;
    this.authentication = authentication;
    this.urlModifier = urlModifier;
  }

  public <T> CompletableFuture<T> get(String url, String contentType, Class<T> type) {
    LOG.info("get {} from {}", type.getName(), url);
    AsyncHttpClient.BoundRequestBuilder requestBuilder = client.prepareGet(urlModifier.apply(url));
    authentication.authenticate(requestBuilder);
    requestBuilder.addHeader("Accept", contentType);
    return execute(requestBuilder, type);
  }

  @Override
  public String getBaseUrl() {
    return urlModifier.apply("");
  }
}
