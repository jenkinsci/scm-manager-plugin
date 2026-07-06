package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.Branch;
import com.cloudogu.scmmanager.scm.api.ExecutionExceptions;
import com.cloudogu.scmmanager.scm.api.Futures;
import com.cloudogu.scmmanager.scm.api.PullRequest;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerObservable;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerTag;
import com.cloudogu.scmmanager.scm.api.Tag;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.trait.SCMSourceTrait;

public class ScmManagerSourceRetriever {

    private final ScmManagerApi api;
    private final Repository repository;
    private final List<SCMSourceTrait> traits;

    private ScmManagerSourceRetriever(ScmManagerApi api, Repository repository, List<SCMSourceTrait> traits) {
        this.api = api;
        this.repository = repository;
        this.traits = traits;
    }

    public Iterable<ScmManagerObservable> getSpecificCandidatesFromSourceControl(
            ScmManagerSourceRequest request, SCMHead head) throws InterruptedException {
        try {
            CompletableFuture<? extends ScmManagerObservable> candidate =
                    getSpecificCandidateFromSourceControl(request, head);
            if (candidate != null) {
                ScmManagerObservable observable = candidate.get();
                if (observable != null) {
                    return Collections.singleton(observable);
                }
            }
        } catch (ExecutionException e) {
            ExecutionExceptions.log(e);
            throw new UncheckedIOException(new IOException("failed to load repository"));
        }

        return Collections.emptySet();
    }

    private CompletableFuture<? extends ScmManagerObservable> getSpecificCandidateFromSourceControl(
            ScmManagerSourceRequest request, SCMHead head) {
        if (head instanceof ScmManagerTag) {
            if (request.isFetchTags()) {
                return api.getTag(repository, head.getName());
            }
        } else if (head instanceof ScmManagerPullRequestHead) {
            if (request.isFetchPullRequests()) {
                CompletableFuture<PullRequest> pullRequest =
                        api.getPullRequest(repository, ((ScmManagerPullRequestHead) head).getId());
                if (pullRequest != null) {
                    return pullRequest.thenApply(this::ignoreDraftPullRequestIfNecessary);
                }
            }
        } else if (head instanceof ScmManagerHead && request.isFetchBranches()) {
            if (shouldIgnoreBranchBecauseRelatedPullRequestExists(head.getName())) {
                return null;
            }
            return api.getBranch(repository, head.getName());
        }

        return null;
    }

    private PullRequest ignoreDraftPullRequestIfNecessary(PullRequest pullRequest) {
        if (shouldExcludeDraftPullRequest(pullRequest)) {
            return null;
        }
        return pullRequest;
    }

    private boolean shouldIgnoreBranchBecauseRelatedPullRequestExists(String branchName) {
        if (excludeBranchesWithPRs()) {
            CompletableFuture<List<PullRequest>> pullRequests = api.getPullRequests(repository);
            return pullRequests.join().stream()
                    .filter(pullRequest -> !shouldExcludeDraftPullRequest(pullRequest))
                    .anyMatch(p -> p.getSource().equals(branchName));
        }
        return false;
    }

    private boolean excludeBranchesWithPRs() {
        return traits.stream()
                .anyMatch(t -> t instanceof PullRequestDiscoveryTrait
                        && ((PullRequestDiscoveryTrait) t).isExcludeBranchesWithPRs());
    }

    private boolean shouldExcludeDraftPullRequest(PullRequest pullRequest) {
        return pullRequest != null && excludeDraftPullRequests() && pullRequest.isDraft();
    }

    private boolean excludeDraftPullRequests() {
        return traits.stream()
                .anyMatch(t -> t instanceof PullRequestDiscoveryTrait
                        && ((PullRequestDiscoveryTrait) t).isExcludeDraftPullRequests());
    }

    public Iterable<ScmManagerObservable> getAllCandidatesFromSourceControl(ScmManagerSourceRequest request)
            throws InterruptedException {
        try {
            CompletableFuture<List<Branch>> branchesFuture = request.isFetchBranches()
                    ? api.getBranches(repository)
                    : CompletableFuture.completedFuture(Collections.emptyList());
            CompletableFuture<List<Tag>> tagsFuture = request.isFetchTags()
                    ? api.getTags(repository)
                    : CompletableFuture.completedFuture(Collections.emptyList());
            CompletableFuture<List<PullRequest>> pullRequestFuture = request.isFetchPullRequests()
                    ? api.getPullRequests(repository).thenApply(this::filterDraftPullRequests)
                    : CompletableFuture.completedFuture(Collections.emptyList());

            CompletableFuture.allOf(branchesFuture, tagsFuture, pullRequestFuture)
                    .join();

            List<ScmManagerObservable> observables = new ArrayList<>();

            observables.addAll(branchesFuture.get());
            observables.addAll(tagsFuture.get());
            observables.addAll(pullRequestFuture.get());

            return observables;
        } catch (ExecutionException e) {
            ExecutionExceptions.log(e);
            throw new UncheckedIOException(new IOException("failed to load repository"));
        }
    }

    private List<PullRequest> filterDraftPullRequests(List<PullRequest> pullRequests) {
        if (!excludeDraftPullRequests()) {
            return pullRequests;
        }
        return pullRequests.stream()
                .filter(pullRequest -> !pullRequest.isDraft())
                .collect(Collectors.toList());
    }

    public ScmManagerApiProbe probe(@NonNull SCMHead head, @CheckForNull SCMRevision revision) {
        ScmManagerRevision rev = null;

        if (revision instanceof ScmManagerPullRequestRevision) {
            rev = ((ScmManagerPullRequestRevision) revision).getSourceRevision();
        } else if (revision instanceof ScmManagerRevision) {
            rev = (ScmManagerRevision) revision;
        } else {
            throw new IllegalArgumentException("unknown type of revision " + revision);
        }

        return new ScmManagerApiProbe(api, repository, head, rev);
    }

    static ScmManagerSourceRetriever create(
            ScmManagerApi api, String namespace, String name, List<SCMSourceTrait> traits) {
        return new ScmManagerSourceRetriever(api, Futures.resolveUnchecked(api.getRepository(namespace, name)), traits);
    }
}
