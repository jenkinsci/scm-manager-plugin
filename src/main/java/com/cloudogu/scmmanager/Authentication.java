package com.cloudogu.scmmanager;

import com.ning.http.client.AsyncHttpClient;

public interface Authentication {

  void authenticate(AsyncHttpClient.BoundRequestBuilder requestBuilder);

}
