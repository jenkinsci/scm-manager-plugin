package com.cloudogu.scmmanager.scm.api;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public final class ExecutionExceptions {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionExceptions.class);

    private ExecutionExceptions() {
    }

    public static void log(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof JsonParseException || cause instanceof JsonMappingException) {
            LOG.warn("could not parse response for request", e);
        } else if (cause instanceof IllegalReturnStatusException) {
            LOG.warn("got error in request: {}", e.getMessage());
        } else if (cause instanceof TimeoutException) {
            LOG.warn("request timed out: {}", e.getMessage());
        } else {
            LOG.warn("got unknown exception in request", e);
        }
    }
}
