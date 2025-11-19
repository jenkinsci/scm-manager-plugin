package com.cloudogu.scmmanager;

import static org.mockito.Mockito.verify;

import hudson.util.Secret;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicHttpAuthenticationTest {

    @Mock
    private Request.Builder requestBuilder;

    @Test
    void testAuthenticate() {
        // we use {} as secret, because we are not able to mock a secret (class is final) and {} can be used to create a
        // secret without the usage of a password store
        Secret secret = Secret.fromString("{}");

        BasicHttpAuthentication authenticator = new BasicHttpAuthentication("trillian", secret);
        authenticator.authenticate(requestBuilder);

        verify(requestBuilder).header("Authorization", "Basic dHJpbGxpYW46e30=");
    }
}
