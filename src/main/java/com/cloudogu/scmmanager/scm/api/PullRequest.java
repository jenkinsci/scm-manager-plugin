package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;

public class PullRequest extends HalRepresentation implements ScmManagerObservable {

  private String id;
  private String source;
  private String target;
  private CloneInformation cloneInformation;

  private Branch sourceBranch;
  private Branch targetBranch;

  private ScmManagerPullRequestHead head;

  public PullRequest() {
  }

  void setCloneInformation(CloneInformation cloneInformation) {
    this.cloneInformation = cloneInformation;
  }

  void setSourceBranch(Branch sourceBranch) {
    this.sourceBranch = sourceBranch;
  }

  void setTargetBranch(Branch targetBranch) {
    this.targetBranch = targetBranch;
  }

  public String getId() {
    return id;
  }

  public String getSource() {
    return source;
  }

  public String getTarget() {
    return target;
  }

  @Override
  public ScmManagerPullRequestHead head() {
    if (head == null) {
      head = new ScmManagerPullRequestHead(cloneInformation, id, new ScmManagerHead(cloneInformation, target), new ScmManagerHead(cloneInformation, source));
    }
    return head;
  }

  @Override
  public ScmManagerPullRequestRevision revision() {
    return new ScmManagerPullRequestRevision(head(), targetBranch.revision(), sourceBranch.revision());
  }
}
