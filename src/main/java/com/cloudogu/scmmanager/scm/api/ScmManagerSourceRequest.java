package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.scm.ScmManagerSource;
import com.cloudogu.scmmanager.scm.ScmManagerSourceContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import jenkins.scm.api.trait.SCMSourceRequest;

public class ScmManagerSourceRequest extends SCMSourceRequest {

    private final boolean fetchBranches;
    private final boolean fetchTags;
    private final boolean fetchPullRequests;

    /**
     * Constructor.
     *
     * @param source   the source.
     * @param context  the context.
     * @param listener the (optional) {@link TaskListener}.
     */
    protected ScmManagerSourceRequest(@NonNull ScmManagerSource source, @NonNull ScmManagerSourceContext context, TaskListener listener) {
        super(source, context, listener);
        this.fetchBranches = context.wantBranches();
        this.fetchTags = context.wantTags();
        this.fetchPullRequests = context.wantPullRequests();
    }

    public boolean isFetchBranches() {
        return fetchBranches;
    }

    public boolean isFetchTags() {
        return fetchTags;
    }

    public boolean isFetchPullRequests() {
        return fetchPullRequests;
    }
}
