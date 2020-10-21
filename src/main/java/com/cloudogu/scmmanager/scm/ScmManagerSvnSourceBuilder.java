package com.cloudogu.scmmanager.scm;

import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.trait.SCMSourceBuilder;

public class ScmManagerSvnSourceBuilder extends SCMSourceBuilder<ScmManagerSvnSourceBuilder, ScmManagerSvnSource> {

  private final String serverUrl;
  private final String repository;
  private final String credentialsId;

  private String id;
  private String includes;
  private String excludes;

  public ScmManagerSvnSourceBuilder(String projectName, String serverUrl, String repository, String credentialsId) {
    super(ScmManagerSvnSource.class, projectName);
    this.serverUrl = serverUrl;
    this.repository = repository;
    this.credentialsId = credentialsId;
  }

  public ScmManagerSvnSourceBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public ScmManagerSvnSourceBuilder withIncludes(String includes) {
    this.includes = includes;
    return this;
  }

  public ScmManagerSvnSourceBuilder withExcludes(String excludes) {
    this.excludes = excludes;
    return this;
  }

  @NonNull
  @Override
  public ScmManagerSvnSource build() {
    ScmManagerSvnSource source = new ScmManagerSvnSource(id, serverUrl, repository, credentialsId);
    if (!Strings.isNullOrEmpty(includes)) {
      source.setIncludes(includes);
    }
    if (!Strings.isNullOrEmpty(excludes)) {
      source.setExcludes(excludes);
    }
    source.setTraits(traits());
    return source;
  }
}
