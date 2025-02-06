package com.cloudogu.scmmanager;

import hudson.util.Secret;
import okhttp3.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BasicHttpAuthenticationTest {

    @Mock
    private Request.Builder requestBuilder;

    @Test
    public void testAuthenticate() {
        // we use {} as secret, because we are not able to mock a secret (class is final) and {} can be used to create a
        // secret without the usage of a password store
        Secret secret = Secret.fromString("{}");

        BasicHttpAuthentication authenticator = new BasicHttpAuthentication("trillian", secret);
        authenticator.authenticate(requestBuilder);

        verify(requestBuilder).header("Authorization", "Basic dHJpbGxpYW46e30=");
    }

}
