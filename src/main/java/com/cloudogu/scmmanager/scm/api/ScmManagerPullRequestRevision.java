package com.cloudogu.scmmanager.scm.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.mixin.ChangeRequestSCMRevision;

public class ScmManagerPullRequestRevision extends ChangeRequestSCMRevision<ScmManagerPullRequestHead> {

    private static final long serialVersionUID = 1L;

    private final ScmManagerRevision sourceRevision;

    public ScmManagerPullRequestRevision(@NonNull ScmManagerPullRequestHead pullRequest, @NonNull ScmManagerRevision targetRevision, @NonNull ScmManagerRevision sourceRevision) {
        super(pullRequest, targetRevision);
        this.sourceRevision = sourceRevision;
    }

    public ScmManagerRevision getSourceRevision() {
        return sourceRevision;
    }

    @Override
    public boolean equivalent(ChangeRequestSCMRevision<?> o) {
        if (!(o instanceof ScmManagerPullRequestRevision)) {
            return false;
        }
        ScmManagerPullRequestRevision other = (ScmManagerPullRequestRevision) o;
        return getHead().equals(other.getHead())
            && sourceRevision.equals(other.sourceRevision);
    }

    @Override
    protected int _hashCode() {
        return sourceRevision.hashCode();
    }

    @Override
    public String toString() {
        return sourceRevision.getRevision();
    }
}
