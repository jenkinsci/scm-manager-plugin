package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ScmManagerObservable;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMSourceRequest;

public class ScmManagerSourceRequest extends SCMSourceRequest {

    private final boolean fetchBranches;
    private final boolean fetchTags;
    private final boolean fetchPullRequests;

    private final List<ScmManagerPullRequestHead> pullRequests = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param source   the source.
     * @param context  the context.
     * @param listener the (optional) {@link TaskListener}.
     */
    protected ScmManagerSourceRequest(
            @NonNull ScmManagerSource source, @NonNull ScmManagerSourceContext context, TaskListener listener) {
        super(source, context, listener);
        this.fetchBranches = context.wantBranches();
        this.fetchTags = context.wantTags();
        this.fetchPullRequests = context.wantPullRequests();
        collectPullRequests(getIncludes(context));
    }

    private void collectPullRequests(Collection<SCMHead> includes) {
        for (SCMHead include : includes) {
            if (include instanceof ScmManagerPullRequestHead) {
                ScmManagerPullRequestHead pr = (ScmManagerPullRequestHead) include;
                pullRequests.add(pr);
            }
        }
    }

    @NonNull
    private Set<SCMHead> getIncludes(@NonNull ScmManagerSourceContext context) {
        Set<SCMHead> includes = context.observer().getIncludes();
        if (includes == null) {
            return Collections.emptySet();
        }
        return includes;
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

    public List<ScmManagerPullRequestHead> getPullRequests() {
        return pullRequests;
    }

    public void prepareForFullScan(Iterable<ScmManagerObservable> observables) {
        List<SCMHead> includes = new ArrayList<>();
        for (ScmManagerObservable observable : observables) {
            includes.add(observable.head());
        }
        collectPullRequests(includes);
    }
}
