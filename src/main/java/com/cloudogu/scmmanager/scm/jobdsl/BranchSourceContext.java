package com.cloudogu.scmmanager.scm.jobdsl;

import com.google.common.base.Strings;
import javaposse.jobdsl.dsl.Preconditions;

public class BranchSourceContext extends ScmManagerContext {

  private String id;
  private String repository;

  public String getId() {
    return id;
  }

  public void id(String id) {
    this.id = id;
  }

  public String getRepository() {
    return repository;
  }

  public void repository(String repository) {
    this.repository = repository;
  }

  public void validate() {
    super.validate();
    Preconditions.checkNotNullOrEmpty(id, "id is required");
    Preconditions.checkNotNullOrEmpty(repository, "repository is required");
  }

}
