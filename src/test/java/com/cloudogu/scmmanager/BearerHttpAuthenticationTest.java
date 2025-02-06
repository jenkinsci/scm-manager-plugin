package com.cloudogu.scmmanager;

import okhttp3.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BearerHttpAuthenticationTest {

    @Mock
    private Request.Builder requestBuilder;

    @Test
    public void shouldAppendBearerHeader() {
        BearerHttpAuthentication authentication = new BearerHttpAuthentication("abc42");

        authentication.authenticate(requestBuilder);

        verify(requestBuilder).addHeader("Authorization", "Bearer abc42");
    }

}
