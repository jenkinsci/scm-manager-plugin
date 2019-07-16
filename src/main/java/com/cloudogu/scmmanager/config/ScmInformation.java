package com.cloudogu.scmmanager.config;

import java.net.URL;

public class ScmInformation {

  private final String type;
  private final URL url;
  private final String revision;
  private final String credentialsId;

  public ScmInformation(String type, URL url, String revision, String credentialsId) {
    this.type = type;
    this.url = url;
    this.revision = revision;
    this.credentialsId = credentialsId;
  }

  public String getType() {
    return type;
  }

  public URL getUrl() {
    return url;
  }

  public String getRevision() {
    return revision;
  }

  public String getCredentialsId() {
    return credentialsId;
  }
}
