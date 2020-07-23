package com.cloudogu.scmmanager.info;

public class JobInformation {

  private final String type;
  private final String url;
  private final String revision;
  private final String credentialsId;
  private final boolean pullRequest;

  public JobInformation(String type, String url, String revision, String credentialsId, boolean pullRequest) {
    this.type = type;
    this.url = url;
    this.revision = revision;
    this.credentialsId = credentialsId;
    this.pullRequest = pullRequest;
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

  public boolean isPullRequest() {
    return pullRequest;
  }
}
