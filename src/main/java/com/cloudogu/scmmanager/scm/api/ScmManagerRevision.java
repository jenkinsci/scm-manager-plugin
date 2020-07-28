package com.cloudogu.scmmanager.scm.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMRevision;

public class ScmManagerRevision extends SCMRevision {

  private final String revision;

  public ScmManagerRevision(@NonNull ScmManagerHead head, @NonNull String revision) {
    super(head);
    this.revision = revision;
  }

  public String getRevision() {
    return revision;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ScmManagerRevision that = (ScmManagerRevision) o;

    return revision.equals(that.revision);
  }

  @Override
  public int hashCode() {
    return revision.hashCode();
  }

  @Override
  public String toString() {
    return revision;
  }
}
