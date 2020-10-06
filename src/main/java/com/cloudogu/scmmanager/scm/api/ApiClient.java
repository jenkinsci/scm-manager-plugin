package com.cloudogu.scmmanager.scm.api;

import java.util.concurrent.CompletableFuture;

public abstract class ApiClient {

  public abstract <T> CompletableFuture<T> get(String url, String contentType, Class<T> type);

}
