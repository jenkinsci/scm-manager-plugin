package com.cloudogu.scmmanager;

import com.ning.http.client.AsyncHttpClient;

public interface HttpAuthentication {

  void authenticate(AsyncHttpClient.BoundRequestBuilder requestBuilder);

}
