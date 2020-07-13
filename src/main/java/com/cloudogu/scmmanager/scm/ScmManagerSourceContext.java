package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.trait.SCMSourceContext;

public class ScmManagerSourceContext extends SCMSourceContext<ScmManagerSourceContext, ScmManagerSourceRequest> {

  private boolean wantBranches;
  private boolean wantTags;
  private boolean wantPullRequests;

  public ScmManagerSourceContext(@CheckForNull SCMSourceCriteria criteria,
                                 @NonNull SCMHeadObserver observer) {
    super(criteria, observer);
  }

  public boolean wantBranches() {
    return wantBranches;
  }

  public boolean wantTags() {
    return wantTags;
  }

  public boolean wantPullRequests() {
    return wantPullRequests;
  }


  public ScmManagerSourceContext wantBranches(boolean include) {
    wantBranches = wantBranches || include;
    return this;
  }

  public ScmManagerSourceContext wantTags(boolean include) {
    wantTags = wantTags || include;
    return this;
  }

  public ScmManagerSourceContext wantPullRequests(boolean include) {
    wantPullRequests = wantPullRequests || include;
    return this;
  }

  @NonNull
  @Override
  public ScmManagerSourceRequest newRequest(@NonNull SCMSource source, TaskListener listener) {
    return new ScmManagerSourceRequest((ScmManagerSource) source, this, listener);
  }
}
