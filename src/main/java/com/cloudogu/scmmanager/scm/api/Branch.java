package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;

import java.util.Objects;

public class Branch extends HalRepresentation implements ScmManagerObservable {

  private CloneInformation cloneInformation;

  private String name;
  private String revision;

  private ScmManagerHead head;

  Branch() {
  }

  public Branch(String name, String revision) {
    this.name = name;
    this.revision = revision;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Branch branch = (Branch) o;
    return Objects.equals(cloneInformation, branch.cloneInformation) &&
      Objects.equals(name, branch.name) &&
      Objects.equals(revision, branch.revision) &&
      Objects.equals(head, branch.head);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), cloneInformation, name, revision, head);
  }
}
