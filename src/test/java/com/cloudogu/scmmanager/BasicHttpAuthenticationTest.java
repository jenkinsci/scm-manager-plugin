package com.cloudogu.scmmanager;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Realm;
import hudson.util.Secret;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BasicHttpAuthenticationTest {

  @Mock
  private AsyncHttpClient.BoundRequestBuilder requestBuilder;

  @Captor
  private ArgumentCaptor<Realm> realmCaptor;

  @Test
  public void testAuthenticate() {
    // we use {} as secret, because we are not able to mock a secret (class is final) and {} can be used to create a
    // secret without the usage of a password store
    Secret secret = Secret.fromString("{}");

    BasicHttpAuthentication authenticator = new BasicHttpAuthentication("trillian", secret);
    authenticator.authenticate(requestBuilder);

    verify(requestBuilder).setRealm(realmCaptor.capture());

    Realm realm = realmCaptor.getValue();
    assertEquals("trillian", realm.getPrincipal());
    assertEquals("{}", realm.getPassword());
  }

}
