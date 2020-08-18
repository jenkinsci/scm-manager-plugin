package com.cloudogu.scmmanager.scm.api;

import jenkins.scm.api.SCMFile;

public class ScmManagerFile {
  private final String path;
  private final SCMFile.Type type;

  public ScmManagerFile(String path, SCMFile.Type type) {
    this.path = path;
    this.type = type;
  }

  public String getPath() {
    return path;
  }

  public SCMFile.Type getType() {
    return type;
  }
}
