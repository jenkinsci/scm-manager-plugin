package com.cloudogu.scmmanager;

import com.google.common.annotations.VisibleForTesting;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Realm;
import hudson.util.Secret;

public class BasicAuthentication implements Authentication {

  private final String username;
  private final Secret password;

  BasicAuthentication(String username, Secret password) {
    this.username = username;
    this.password = password;
  }

  @VisibleForTesting
  String getUsername() {
    return username;
  }

  @VisibleForTesting
  Secret getPassword() {
    return password;
  }

  @Override
  public void authenticate(AsyncHttpClient.BoundRequestBuilder requestBuilder) {
    Realm realm = new Realm.RealmBuilder()
      .setUsePreemptiveAuth(true)
      .setScheme(Realm.AuthScheme.BASIC)
      .setPrincipal(username)
      .setPassword(password.getPlainText())
      .build();
    requestBuilder.setRealm(realm);
  }
}
