package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ApiClient;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScmManagerApiTestMocks {

  public static <T> void mockResult(OngoingStubbing<ApiClient.Promise<T>> when, T... results) {
    ApiClient.Promise<T>[] promises = Arrays.stream(results)
      .map(result -> CompletableFuture.completedFuture(result))
      .map(future -> new ApiClient.Promise<>(future))
      .toArray(n -> new ApiClient.Promise[n]);
    ApiClient.Promise<T>[] furtherResults =
      promises.length == 0? new ApiClient.Promise[]{}: Arrays.copyOfRange(promises, 1, promises.length);
    when.thenReturn(promises[0], furtherResults);
  }

  public static <T> void mockError(ApiClient.ApiError apiError, OngoingStubbing<ApiClient.Promise<T>> stubbing) throws InterruptedException {
    ApiClient.Promise<?> promise = mock(ApiClient.Promise.class);
    stubbing.thenReturn((ApiClient.Promise<T>) promise);
    when(promise.then(any())).thenReturn((ApiClient.Promise<Object>) promise);
    doAnswer(invocation -> invocation.getArgument(0, Function.class).apply(apiError)).when(promise).mapError(any());
  }
}
