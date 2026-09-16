package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PullRequest extends HalRepresentation implements ScmManagerObservable {

    private String id;

    private String source;

    private String target;

    private String status;

    private List<String> labels = Collections.emptyList();

    private String title;

    private CloneInformation cloneInformation;

    private Branch sourceBranch;
    private Branch targetBranch;

    private ScmManagerPullRequestHead head;

    PullRequest() {}

    public PullRequest(String id, Branch targetBranch, Branch sourceBranch, CloneInformation cloneInformation) {
        this(id, targetBranch, sourceBranch, cloneInformation, "OPEN");
    }

    public PullRequest(
            String id, Branch targetBranch, Branch sourceBranch, CloneInformation cloneInformation, String status) {
        this.id = id;
        this.targetBranch = targetBranch;
        this.target = targetBranch.getName();
        this.sourceBranch = sourceBranch;
        this.source = sourceBranch.getName();
        this.cloneInformation = cloneInformation;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDraft() {
        return "DRAFT".equals(status);
    }

    public List<String> getLabels() {
        if (labels == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(labels);
    }

    @Override
    public ScmManagerPullRequestHead head() {
        if (head == null) {
            head = new ScmManagerPullRequestHead(
                    cloneInformation,
                    id,
                    new ScmManagerHead(cloneInformation, target),
                    new ScmManagerHead(cloneInformation, source),
                    title,
                    getLabels()
            );
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
        return Objects.equals(id, that.id)
                && Objects.equals(source, that.source)
                && Objects.equals(target, that.target)
                && Objects.equals(status, that.status)
                && Objects.equals(title, that.title)
                && Objects.equals(getLabels(), that.getLabels())
                && Objects.equals(cloneInformation, that.cloneInformation)
                && Objects.equals(sourceBranch, that.sourceBranch)
                && Objects.equals(targetBranch, that.targetBranch)
                && Objects.equals(head, that.head);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                id,
                source,
                target,
                status,
                title,
                getLabels(),
                cloneInformation,
                sourceBranch,
                targetBranch,
                head);
    }
}
