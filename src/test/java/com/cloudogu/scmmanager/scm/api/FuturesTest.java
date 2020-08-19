package com.cloudogu.scmmanager.scm.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FuturesTest {

  @Mock
  private CompletableFuture<String> future;

  @Test
  public void shouldThrowIOException() throws ExecutionException, InterruptedException {
    when(future.get()).thenThrow(new SimpleExecutionException("failed"));
    try {
      Futures.resolveChecked(future);
    } catch (IOException ex) {
      assertThat(ex.getCause()).isInstanceOf(ExecutionException.class);
    }
  }

  @Test
  public void shouldThrowIOExceptionAndInterruptCurrentThread() throws ExecutionException, InterruptedException {
    when(future.get()).thenThrow(new InterruptedException("failed"));
    try {
      Futures.resolveChecked(future);
    } catch (IOException ex) {
      assertThat(Thread.currentThread().isInterrupted()).isTrue();
      assertThat(ex.getCause()).isInstanceOf(InterruptedException.class);
    }
  }

  @Test
  public void shouldPassWithoutCheckedException() throws IOException {
    String value = Futures.resolveChecked(CompletableFuture.completedFuture("test"));
    assertThat(value).isEqualTo("test");
  }

  @Test
  public void shouldThrowUncheckedIOException() throws ExecutionException, InterruptedException {
    when(future.get()).thenThrow(new SimpleExecutionException("failed"));
    try {
      Futures.resolveUnchecked(future);
    } catch (UncheckedIOException ex) {
      assertThat(ex.getCause()).isInstanceOf(IOException.class);
      assertThat(ex.getCause().getCause()).isInstanceOf(ExecutionException.class);
    }
  }

  @Test
  public void shouldThrowUncheckedIOExceptionAndInterruptCurrentThread() throws ExecutionException, InterruptedException {
    when(future.get()).thenThrow(new InterruptedException("failed"));
    try {
      Futures.resolveUnchecked(future);
    } catch (UncheckedIOException ex) {
      assertThat(Thread.currentThread().isInterrupted()).isTrue();
      assertThat(ex.getCause()).isInstanceOf(IOException.class);
      assertThat(ex.getCause().getCause()).isInstanceOf(InterruptedException.class);
    }
  }

  @Test
  public void shouldPassWithoutUncheckedException() {
    String value = Futures.resolveUnchecked(CompletableFuture.completedFuture("test"));
    assertThat(value).isEqualTo("test");
  }



  private static class SimpleExecutionException extends ExecutionException {
    public SimpleExecutionException(String message) {
      super(message);
    }
  }

}
