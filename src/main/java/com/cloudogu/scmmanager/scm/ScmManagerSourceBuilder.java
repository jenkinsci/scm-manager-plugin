package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.trait.SCMSourceBuilder;

public class ScmManagerSourceBuilder extends SCMSourceBuilder<ScmManagerSourceBuilder, ScmManagerSource> {

  private final String serverUrl;
  private final String repository;
  private final String credentialsId;

  private String id;

  public ScmManagerSourceBuilder(String projectName, String serverUrl, String repository, String credentialsId) {
    super(ScmManagerSource.class, projectName);
    this.serverUrl = serverUrl;
    this.repository = repository;
    this.credentialsId = credentialsId;
  }

  public ScmManagerSourceBuilder withId(String id) {
    this.id = id;
    return this;
  }

  @NonNull
  @Override
  public ScmManagerSource build() {
    ScmManagerSource source = new ScmManagerSource(serverUrl, repository, credentialsId);
    source.setId(id);
    source.setTraits(traits());
    return source;
  }
}
