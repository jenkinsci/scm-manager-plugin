package com.cloudogu.scmmanager.scm.jobdsl;

import com.google.common.base.Strings;
import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.Preconditions;

public class BranchSourceContext implements Context {

  private String id;
  private String serverUrl;
  private String repository;
  private String credentialsId;

  public String getId() {
    return id;
  }

  public void id(String id) {
    this.id = id;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public void serverUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  public String getRepository() {
    return repository;
  }

  public void repository(String repository) {
    this.repository = repository;
  }

  public String getCredentialsId() {
    return credentialsId;
  }

  public void credentialsId(String credentialsId) {
    this.credentialsId = credentialsId;
  }

  public void validate() {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "id is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(serverUrl), "serverUrl is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(repository), "serverUrl is required");
  }

}
