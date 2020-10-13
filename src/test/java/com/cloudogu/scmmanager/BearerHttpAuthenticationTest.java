package com.cloudogu.scmmanager;

import com.ning.http.client.AsyncHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BearerHttpAuthenticationTest {

  @Mock
  private AsyncHttpClient.BoundRequestBuilder requestBuilder;

  @Test
  public void shouldAppendBearerHeader() {
    BearerHttpAuthentication authentication = new BearerHttpAuthentication("abc42");

    authentication.authenticate(requestBuilder);

    verify(requestBuilder).addHeader("Authorization", "Bearer abc42");
  }

}
