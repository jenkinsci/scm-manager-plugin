package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;

public class Tag extends HalRepresentation implements ScmManagerObservable {

  private String name;
  private String revision;
  private Changeset changeset;

  private CloneInformation cloneInformation;
  private ScmManagerTag head;

  public String getName() {
    return name;
  }

  public String getRevision() {
    return revision;
  }

  // TODO can this setter be avoided?
  void setCloneInformation(CloneInformation cloneInformation) {
    this.cloneInformation = cloneInformation;
  }

  void setChangeset(Changeset changeset) {
    this.changeset = changeset;
  }

  public Changeset getChangeset() {
    return changeset;
  }

  @Override
  public ScmManagerTag head() {
    if (head == null) {
      head = new ScmManagerTag(cloneInformation, name, changeset.getDate().getTime());
    }
    return head;
  }

  @Override
  public ScmManagerRevision revision() {
    return new ScmManagerRevision(head(), revision);
  }
}
