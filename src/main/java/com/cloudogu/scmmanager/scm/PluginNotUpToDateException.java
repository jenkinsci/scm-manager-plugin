package com.cloudogu.scmmanager.scm;

public class PluginNotUpToDateException extends RuntimeException {

    public PluginNotUpToDateException(String message) {
        super(message);
    }
}
