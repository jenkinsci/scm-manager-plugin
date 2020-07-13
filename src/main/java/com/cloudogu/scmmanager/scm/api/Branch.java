package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;

public class Branch extends HalRepresentation implements ScmManagerObservable {

  private CloneInformation cloneInformation;

  private String name;
  private String revision;

  private ScmManagerHead head;

  Branch() {
  }

  void setCloneInformation(CloneInformation cloneInformation) {
    this.cloneInformation = cloneInformation;
  }

  public String getName() {
    return name;
  }

  public String getRevision() {
    return revision;
  }

  @Override
  public ScmManagerHead head() {
    if (head == null) {
      head = new ScmManagerHead(cloneInformation, name);
    }
    return head;
  }

  @Override
  public ScmManagerRevision revision() {
    return new ScmManagerRevision(head(), revision);
  }
}
