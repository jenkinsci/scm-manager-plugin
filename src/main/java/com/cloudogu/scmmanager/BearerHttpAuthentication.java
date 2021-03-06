package com.cloudogu.scmmanager;

import com.ning.http.client.AsyncHttpClient;

public class BearerHttpAuthentication implements HttpAuthentication {

  private final String accessToken;

  public BearerHttpAuthentication(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public void authenticate(AsyncHttpClient.BoundRequestBuilder requestBuilder) {
    authenticate(requestBuilder, accessToken);
  }

  public static void authenticate(AsyncHttpClient.BoundRequestBuilder requestBuilder, String accessToken) {
    requestBuilder.addHeader("Authorization", "Bearer ".concat(accessToken));
  }
}
