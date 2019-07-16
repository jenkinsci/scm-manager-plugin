package com.cloudogu.scmmanager.config;

public class ScmInformation {

  private final String type;
  private final String url;
  private final String revision;
  private final String credentialsId;

  public ScmInformation(String type, String url, String revision, String credentialsId) {
    this.type = type;
    this.url = url;
    this.revision = revision;
    this.credentialsId = credentialsId;
  }

  public String getType() {
    return type;
  }

  public String getUrl() {
    return url;
  }

  public String getRevision() {
    return revision;
  }

  public String getCredentialsId() {
    return credentialsId;
  }
}
