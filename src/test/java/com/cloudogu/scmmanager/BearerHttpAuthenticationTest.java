package com.cloudogu.scmmanager;

import static org.mockito.Mockito.verify;

import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BearerHttpAuthenticationTest {

    @Mock
    private Request.Builder requestBuilder;

    @Test
    void shouldAppendBearerHeader() {
        BearerHttpAuthentication authentication = new BearerHttpAuthentication("abc42");

        authentication.authenticate(requestBuilder);

        verify(requestBuilder).addHeader("Authorization", "Bearer abc42");
    }
}
