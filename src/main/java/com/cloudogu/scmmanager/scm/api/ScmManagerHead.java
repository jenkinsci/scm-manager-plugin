package com.cloudogu.scmmanager.scm.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadOrigin;

public class ScmManagerHead extends SCMHead {

  private final CloneInformation cloneInformation;

  public ScmManagerHead(@NonNull CloneInformation cloneInformation, @NonNull String name) {
    super(name);
    this.cloneInformation = cloneInformation;
  }

  public CloneInformation getCloneInformation() {
    return cloneInformation;
  }

  @NonNull
  @Override
  public SCMHeadOrigin getOrigin() {
    return SCMHeadOrigin.DEFAULT;
  }
}
