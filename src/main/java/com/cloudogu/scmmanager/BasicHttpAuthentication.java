package com.cloudogu.scmmanager;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.google.common.annotations.VisibleForTesting;
import hudson.util.Secret;
import okhttp3.Credentials;
import okhttp3.Request;

public class BasicHttpAuthentication implements HttpAuthentication {

  private final String username;
  private final Secret password;

  BasicHttpAuthentication(String username, Secret password) {
    this.username = username;
    this.password = password;
  }

  public static BasicHttpAuthentication from(UsernamePasswordCredentials credentials) {
    return new BasicHttpAuthentication(credentials.getUsername(), credentials.getPassword());
  }

  @VisibleForTesting
  String getUsername() {
    return username;
  }

  @VisibleForTesting
  Secret getPassword() {
    return password;
  }

  public void authenticate(Request.Builder requestBuilder) {
    requestBuilder.header("Authorization", Credentials.basic(username, password.getPlainText()));
  }
}
