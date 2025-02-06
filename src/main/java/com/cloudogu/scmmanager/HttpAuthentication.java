package com.cloudogu.scmmanager;

import okhttp3.Request;

public interface HttpAuthentication {

    void authenticate(Request.Builder requestBuilder);
}
