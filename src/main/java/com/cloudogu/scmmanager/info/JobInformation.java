package com.cloudogu.scmmanager.info;

public class JobInformation {

  private final String type;
  private final String url;
  private final String revision;
  private final String credentialsId;

  public JobInformation(String type, String url, String revision, String credentialsId) {
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
