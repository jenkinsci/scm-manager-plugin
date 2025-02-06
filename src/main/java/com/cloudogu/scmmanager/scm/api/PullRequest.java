package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Objects;

public class PullRequest extends HalRepresentation implements ScmManagerObservable {

    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    private String id;
    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    private String source;
    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    private String target;
    private CloneInformation cloneInformation;

    private Branch sourceBranch;
    private Branch targetBranch;

    private ScmManagerPullRequestHead head;

    PullRequest() {
    }

    public PullRequest(String id, Branch targetBranch, Branch sourceBranch, CloneInformation cloneInformation) {
        this.id = id;
        this.targetBranch = targetBranch;
        this.target = targetBranch.getName();
        this.sourceBranch = sourceBranch;
        this.source = sourceBranch.getName();
        this.cloneInformation = cloneInformation;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PullRequest that = (PullRequest) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(source, that.source) &&
            Objects.equals(target, that.target) &&
            Objects.equals(cloneInformation, that.cloneInformation) &&
            Objects.equals(sourceBranch, that.sourceBranch) &&
            Objects.equals(targetBranch, that.targetBranch) &&
            Objects.equals(head, that.head);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, source, target, cloneInformation, sourceBranch, targetBranch, head);
    }
}
