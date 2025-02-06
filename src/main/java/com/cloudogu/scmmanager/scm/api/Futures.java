package com.cloudogu.scmmanager.scm.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class Futures {

    private Futures() {
    }

    public static <T> T resolveChecked(CompletableFuture<T> future) throws IOException {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw checked(e);
        } catch (ExecutionException e) {
            throw checked(e);
        }
    }

    private static IOException checked(Exception e) {
        return new IOException("failed to fetch", e);
    }

    public static <T> T resolveUnchecked(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unchecked(e);
        } catch (ExecutionException e) {
            throw unchecked(e);
        }
    }

    private static RuntimeException unchecked(Exception e) {
        return new UncheckedIOException("failed to fetch", new IOException(e));
    }

}
