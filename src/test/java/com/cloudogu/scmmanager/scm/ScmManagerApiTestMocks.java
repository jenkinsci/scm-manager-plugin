package com.cloudogu.scmmanager.scm;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.mockito.stubbing.OngoingStubbing;

class ScmManagerApiTestMocks {

    @SafeVarargs
    public static <T> void mockResult(OngoingStubbing<CompletableFuture<T>> when, T... results) {
        CompletableFuture<T>[] futures =
                Arrays.stream(results).map(CompletableFuture::completedFuture).toArray(CompletableFuture[]::new);
        CompletableFuture<T>[] furtherResults =
                futures.length == 0 ? new CompletableFuture[] {} : Arrays.copyOfRange(futures, 1, futures.length);
        when.thenReturn(futures[0], furtherResults);
    }

    public static <T> void mockError(Throwable apiError, OngoingStubbing<CompletableFuture<T>> stubbing)
            throws Exception {
        CompletableFuture<T> promise = mock(CompletableFuture.class);
        stubbing.thenReturn(promise);
        lenient().when(promise.thenApply(any())).thenReturn((CompletableFuture<Object>) promise);
        doAnswer(invocation -> CompletableFuture.completedFuture(
                        invocation.getArgument(0, Function.class).apply(apiError)))
                .when(promise)
                .exceptionally(any());
        lenient().when(promise.get()).thenThrow(apiError);
    }
}
