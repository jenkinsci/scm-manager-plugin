package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class ScmManagerApi {

  private final ApiClient client;

  public ScmManagerApi(ApiClient client) {
    this.client = client;
  }

  public CompletableFuture<HalRepresentation> index() {
    return client.get("/api/v2", "application/vnd.scmm-index+json;v=2", HalRepresentation.class);
  }

  public CompletableFuture<List<Repository>> getRepositories() {
    // TODO pageSize?
    return client.get("/api/v2/repositories?pageSize=2000&sortBy=namespace&sortBy=name", "application/vnd.scmm-repositoryCollection+json;v=2", RepositoryCollection.class)
      .thenApply(collection -> collection.get_embedded().getRepositories());
  }

  public CompletableFuture<Repository> getRepository(String namespace, String name) {
    String url = String.format("/api/v2/repositories/%s/%s", namespace, name);
    return client.get(url, "application/vnd.scmm-repository+json;v=2", Repository.class);
  }

  public CompletableFuture<List<Branch>> getBranches(Repository repository) {
    Optional<Link> branchesLink = repository.getLinks().getLinkBy("branches");
    if (branchesLink.isPresent()) {
      return client.get(branchesLink.get().getHref(), "application/vnd.scmm-branchCollection+json;v=2", BranchCollection.class)
        .thenApply(branchCollection -> branchCollection.get_embedded().getBranches())
        .thenApply(branches -> {
            branches.forEach(branch -> branch.setCloneInformation(repository.getCloneInformation()));
            return branches;
          }
        );
    }
    return CompletableFuture.completedFuture(emptyList());
  }

  public CompletableFuture<List<Tag>> getTags(Repository repository) {
    Optional<Link> tagsLink = repository.getLinks().getLinkBy("tags");
    if (tagsLink.isPresent()) {
      return client.get(tagsLink.get().getHref(), "application/vnd.scmm-tagCollection+json;v=2", TagCollection.class)
        .thenApply(tags -> tags.get_embedded().getTags().stream()
          .map(tag -> {
            Optional<Link> changesetLink = tag.getLinks().getLinkBy("changeset");
            if (changesetLink.isPresent()) {
              return client.get(changesetLink.get().getHref(), "application/vnd.scmm-changeset+json;v=2", Changeset.class)
                .thenApply(changeset -> {
                  tag.setCloneInformation(repository.getCloneInformation());
                  tag.setChangeset(changeset);
                  return tag;
                });
            }
            throw new IllegalStateException("could not find changeset link on tag " + tag.getName());
          })
          .collect(Collectors.toList()))
        .thenCompose(completableFutures -> CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
          .thenApply(future -> completableFutures.stream().filter(cf -> !cf.isCompletedExceptionally()).map(CompletableFuture::join)
            .collect(Collectors.toList())));
    }
    return CompletableFuture.completedFuture(emptyList());
  }

  public CompletableFuture<List<PullRequest>> getPullRequests(Repository repository) {
    Optional<Link> pullRequestLink = repository.getLinks().getLinkBy("pullRequest");
    if (pullRequestLink.isPresent()) {
      return client.get(pullRequestLink.get().getHref() + "?status=OPEN", "application/vnd.scmm-pullRequest+json;v=2", PullRequestCollection.class)
        .thenApply(
          pullRequestCollection -> pullRequestCollection.get_embedded().getPullRequests().stream()
            .map(pullRequest -> {
              pullRequest.setCloneInformation(repository.getCloneInformation());
              // TODO we need the information or the links on the pull request object

              CompletableFuture<Void> source = client.get(getLink(pullRequest, "sourceBranch"), "application/vnd.scmm-branch+json;v=2", Branch.class).thenAccept(pullRequest::setSourceBranch);
              CompletableFuture<Void> target = client.get(getLink(pullRequest, "targetBranch"), "application/vnd.scmm-branch+json;v=2", Branch.class).thenAccept(pullRequest::setTargetBranch);

              return CompletableFuture.allOf(source, target).thenApply(v -> pullRequest);
            })
            .collect(Collectors.toList()))
        .thenCompose(completableFutures -> CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
          .thenApply(future -> completableFutures.stream().filter(cf -> !cf.isCompletedExceptionally()).map(CompletableFuture::join)
            .collect(Collectors.toList())));
    }
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  private String getLink(HalRepresentation hal, String linkName) {
    return hal.getLinks().getLinkBy(linkName)
      .orElseThrow(() -> new RuntimeException("could not find link '" + linkName + "'"))
      .getHref();
  }

  private static class RepositoryCollection {
    private EmbeddedRepositories _embedded;

    public EmbeddedRepositories get_embedded() {
      return _embedded;
    }
  }

  private static class EmbeddedRepositories {
    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    private List<Repository> repositories;

    public List<Repository> getRepositories() {
      return repositories;
    }
  }

  private static class BranchCollection {
    private EmbeddedBranches _embedded;

    public EmbeddedBranches get_embedded() {
      return _embedded;
    }
  }

  private static class EmbeddedBranches {
    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    private List<Branch> branches;

    public List<Branch> getBranches() {
      return branches;
    }
  }

  private static class TagCollection {
    private EmbeddedTags _embedded;

    public EmbeddedTags get_embedded() {
      return _embedded;
    }
  }

  private static class EmbeddedTags {
    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    private List<Tag> tags;

    public List<Tag> getTags() {
      return tags;
    }
  }

  private static class PullRequestCollection {
    private EmbeddedPullRequests _embedded;

    public EmbeddedPullRequests get_embedded() {
      return _embedded;
    }
  }

  private static class EmbeddedPullRequests {
    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    private List<PullRequest> pullRequests;

    public List<PullRequest> getPullRequests() {
      return pullRequests;
    }
  }
}
