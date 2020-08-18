package com.cloudogu.scmmanager.scm.api;

public class IllegalReturnStatusException extends RuntimeException {

  private final int statusCode;

  public IllegalReturnStatusException(int statusCode) {
    super("illegal status code returned: " + statusCode);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
