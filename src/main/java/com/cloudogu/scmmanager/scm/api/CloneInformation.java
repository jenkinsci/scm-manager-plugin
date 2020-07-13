package com.cloudogu.scmmanager.scm.api;

public class CloneInformation {

  private final String type;
  private final String url;

  public CloneInformation(String type, String url) {
    this.type = type;
    this.url = url;
  }

  public String getType() {
    return type;
  }

  public String getUrl() {
    return url;
  }
}
