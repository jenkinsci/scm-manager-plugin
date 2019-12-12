package com.cloudogu.scmmanager;

public class SshConnectionFailedException extends RuntimeException {
  public SshConnectionFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
