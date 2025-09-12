package com.cloudogu.scmmanager.scm.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FuturesTest {

    @Mock
    private CompletableFuture<String> future;

    @Test
    void shouldThrowIOException() throws Exception {
        when(future.get()).thenThrow(new SimpleExecutionException("failed"));
        try {
            Futures.resolveChecked(future);
        } catch (IOException ex) {
            assertThat(ex.getCause()).isInstanceOf(ExecutionException.class);
        }
    }

    @Test
    void shouldThrowIOExceptionAndInterruptCurrentThread() throws Exception {
        when(future.get()).thenThrow(new InterruptedException("failed"));
        runInThread(() -> {
            try {
                Futures.resolveUnchecked(future);
            } catch (UncheckedIOException ex) {
                assertThat(Thread.currentThread().isInterrupted()).isTrue();
                assertThat(ex.getCause()).isInstanceOf(IOException.class);
                assertThat(ex.getCause().getCause()).isInstanceOf(InterruptedException.class);
            }
        });
    }

    @Test
    void shouldPassWithoutCheckedException() throws IOException {
        String value = Futures.resolveChecked(CompletableFuture.completedFuture("test"));
        assertThat(value).isEqualTo("test");
    }

    @Test
    void shouldThrowUncheckedIOException() throws Exception {
        when(future.get()).thenThrow(new SimpleExecutionException("failed"));
        try {
            Futures.resolveUnchecked(future);
        } catch (UncheckedIOException ex) {
            assertThat(ex.getCause()).isInstanceOf(IOException.class);
            assertThat(ex.getCause().getCause()).isInstanceOf(ExecutionException.class);
        }
    }

    @Test
    void shouldThrowUncheckedIOExceptionAndInterruptCurrentThread() throws Exception {
        when(future.get()).thenThrow(new InterruptedException("failed"));
        runInThread(() -> {
            try {
                Futures.resolveUnchecked(future);
            } catch (UncheckedIOException ex) {
                assertThat(Thread.currentThread().isInterrupted()).isTrue();
                assertThat(ex.getCause()).isInstanceOf(IOException.class);
                assertThat(ex.getCause().getCause()).isInstanceOf(InterruptedException.class);
            }
        });
    }

    @Test
    void shouldPassWithoutUncheckedException() {
        String value = Futures.resolveUnchecked(CompletableFuture.completedFuture("test"));
        assertThat(value).isEqualTo("test");
    }

    private static class SimpleExecutionException extends ExecutionException {
        public SimpleExecutionException(String message) {
            super(message);
        }
    }

    // logic to run tests in a separate thread
    // this is required to not interrupt the main test thread, which could cause problems with wiremock/jetty

    private ExecutorService executor;
    private AssertionError assertionError;

    @BeforeEach
    void beforeEach() {
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void afterEach() {
        executor.shutdown();
    }

    public void runInThread(Runnable runnable) {
        Future<?> future = executor.submit(capture(runnable));
        waitTillFinished(future);
        if (assertionError != null) {
            throw assertionError;
        }
    }

    private Runnable capture(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (AssertionError assertionError) {
                this.assertionError = assertionError;
            }
        };
    }

    private void waitTillFinished(Future<?> future) {
        try {
            future.get();
        } catch (ExecutionException | InterruptedException shouldNotHappen) {
            throw new IllegalStateException(shouldNotHappen);
        }
    }
}
