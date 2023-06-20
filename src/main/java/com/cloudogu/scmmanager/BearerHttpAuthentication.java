package com.cloudogu.scmmanager;

import okhttp3.Request;

public class BearerHttpAuthentication implements HttpAuthentication {

  private final String accessToken;

  public BearerHttpAuthentication(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public void authenticate(Request.Builder requestBuilder) {
    authenticate(requestBuilder, accessToken);
  }

  public static void authenticate(Request.Builder requestBuilder, String accessToken) {
    requestBuilder.addHeader("Authorization", "Bearer ".concat(accessToken));
  }
}
