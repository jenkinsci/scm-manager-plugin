package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;
import java.util.Objects;

public class Tag extends HalRepresentation implements ScmManagerObservable {

    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    private String name;
    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    private String revision;

    private Date date;

    private CloneInformation cloneInformation;
    private ScmManagerTag head;

    Tag() {
    }

    public Tag(String name, String revision, Changeset changeset, CloneInformation cloneInformation) {
        this.name = name;
        this.revision = revision;
        this.date = changeset.getDate();
        this.cloneInformation = cloneInformation;
    }

    public String getName() {
        return name;
    }

    public String getRevision() {
        return revision;
    }

    public Date getDate() {
        return date == null ? null : new Date(date.getTime());
    }

    CloneInformation getCloneInformation() {
        return cloneInformation;
    }

    // TODO can this setter be avoided?
    void setCloneInformation(CloneInformation cloneInformation) {
        this.cloneInformation = cloneInformation;
    }

    void setChangeset(Changeset changeset) {
        this.date = changeset.getDate();
    }

    @Override
    public ScmManagerTag head() {
        if (head == null) {
            head = new ScmManagerTag(cloneInformation, name, date.getTime());
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
        Tag tag = (Tag) o;
        return Objects.equals(name, tag.name) &&
            Objects.equals(revision, tag.revision) &&
            Objects.equals(date, tag.date) &&
            Objects.equals(cloneInformation, tag.cloneInformation) &&
            Objects.equals(head, tag.head);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, revision, date, cloneInformation, head);
    }
}
