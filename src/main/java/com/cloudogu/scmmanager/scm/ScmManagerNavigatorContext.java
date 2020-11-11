package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.trait.SCMNavigatorContext;
import jenkins.scm.api.trait.SCMNavigatorRequest;

public class ScmManagerNavigatorContext extends SCMNavigatorContext<ScmManagerNavigatorContext, SCMNavigatorRequest> {

  private String svnIncludes;
  private String svnExcludes;

  public String getSvnIncludes() {
    return svnIncludes;
  }

  public void setSvnIncludes(String svnIncludes) {
    this.svnIncludes = svnIncludes;
  }

  public String getSvnExcludes() {
    return svnExcludes;
  }

  public void setSvnExcludes(String svnExcludes) {
    this.svnExcludes = svnExcludes;
  }

  @NonNull
  @Override
  public ScmManagerNavigatorRequest newRequest(@NonNull SCMNavigator navigator, @NonNull SCMSourceObserver observer) {
    return new ScmManagerNavigatorRequest(navigator, this, observer);
  }
}
