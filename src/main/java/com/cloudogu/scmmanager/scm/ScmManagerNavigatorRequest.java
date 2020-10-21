package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.trait.SCMNavigatorRequest;

public class ScmManagerNavigatorRequest extends SCMNavigatorRequest {

  private final String svnIncludes;
  private final String svnExcludes;

  protected ScmManagerNavigatorRequest(@NonNull SCMNavigator source, @NonNull ScmManagerNavigatorContext context, @NonNull SCMSourceObserver observer) {
    super(source, context, observer);
    this.svnIncludes = context.getSvnIncludes();
    this.svnExcludes = context.getSvnExcludes();
  }

  public String getSvnIncludes() {
    return svnIncludes;
  }

  public String getSvnExcludes() {
    return svnExcludes;
  }
}
