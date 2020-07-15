package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.scm.api.ApiClient.Promise;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class ScmManagerApi {

  private static final Logger LOG = LoggerFactory.getLogger(ScmManagerApi.class);

  private final ApiClient client;

  public ScmManagerApi(ApiClient client) {
    this.client = client;
  }

  public Promise<HalRepresentation> index() {
    return client.get("/api/v2", "application/vnd.scmm-index+json;v=2", HalRepresentation.class);
  }

  public Promise<List<Repository>> getRepositories() {
    // TODO pageSize?
    return client.get("/api/v2/repositories?pageSize=2000&sortBy=namespace&sortBy=name", "application/vnd.scmm-repositoryCollection+json;v=2", RepositoryCollection.class)
      .then(collection -> collection.get_embedded().getRepositories());
  }

  public Promise<Repository> getRepository(String namespace, String name) {
    String url = String.format("/api/v2/repositories/%s/%s", namespace, name);
    return client.get(url, "application/vnd.scmm-repository+json;v=2", Repository.class)
      .then(repository -> {
        repository.setClient(client);
        return repository;
      });
  }

  public Promise<List<Branch>> getBranches(Repository repository) {
    Optional<Link> branchesLink = repository.getLinks().getLinkBy("branches");
    if (branchesLink.isPresent()) {
      return client.get(branchesLink.get().getHref(), "application/vnd.scmm-branchCollection+json;v=2", BranchCollection.class)
        .then(branchCollection -> branchCollection.get_embedded().getBranches())
        .then(branches -> {
            branches.forEach(branch -> branch.setCloneInformation(repository.getCloneInformation()));
            return branches;
          }
        );
    }
    return new Promise<>(emptyList());
  }

  public Promise<List<Tag>> getTags(Repository repository) {
    Optional<Link> tagsLink = repository.getLinks().getLinkBy("tags");
    if (tagsLink.isPresent()) {
      return client.get(tagsLink.get().getHref(), "application/vnd.scmm-tagCollection+json;v=2", TagCollection.class)
        .then(tagCollection -> tagCollection.get_embedded().getTags())
        .then(tags -> {
          tags.forEach(t -> t.setCloneInformation(repository.getCloneInformation()));
          tags.forEach(tag -> setChangeset(tag));
          return tags;
        });
    }
    return new Promise<>(emptyList());
  }

  private void setChangeset(Tag tag) {
    Promise<Changeset> changeset = getChangeset(tag);
    if (changeset == null) {
      return;
    }
    try {
      changeset.then(c -> {
        tag.setChangeset(c);
        return null;
      }).mapError(e -> {
        LOG.warn("could not load changeset for tag {}: {}", tag.getName(), e.getMessage());
        return null;
      });
    } catch (InterruptedException e) {
      LOG.warn("interrupted loading changeset for tag {}", tag.getName(), e);
    }
  }

  private Promise<Changeset> getChangeset(Tag tag) {
    return tag.getLinks().getLinkBy("changeset")
      .map(link -> client.get(link.getHref(), "application/vnd.scmm-changeset+json;v=2", Changeset.class))
      .orElse(null);
  }

  public Promise<List<PullRequest>> getPullRequests(Repository repository) {
    Optional<Link> pullRequestLink = repository.getLinks().getLinkBy("pullRequest");
    if (pullRequestLink.isPresent()) {
      return client.get(pullRequestLink.get().getHref() + "?status=OPEN", "application/vnd.scmm-pullRequest+json;v=2", PullRequestCollection.class)
        .then(pullRequestCollection -> pullRequestCollection.get_embedded().getPullRequests())
        .then(pullRequests -> pullRequests.stream().peek(pullRequest -> {
          pullRequest.setCloneInformation(repository.getCloneInformation());
          try {
            pullRequest.setTargetBranch(
              client.get(getLink(pullRequest, "targetBranch"), "application/vnd.scmm-branch+json;v=2", Branch.class)
                .mapError(e -> {
                  LOG.error("Could not load target branch information for pull request: {}", e.getMessage());
                  return null;
                }));
            pullRequest.setSourceBranch(
              client.get(getLink(pullRequest, "sourceBranch"), "application/vnd.scmm-branch+json;v=2", Branch.class)
                .mapError(e -> {
                  LOG.error("Could not load source branch information for pull request: {}", e.getMessage());
                  return null;
                }));
          } catch (InterruptedException e) {
            LOG.error("Could not load branch information for pull request", e);
          }
        }).collect(toList()));
    }
    return new Promise<>(Collections.emptyList());
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
    private List<PullRequest> pullRequests;

    public List<PullRequest> getPullRequests() {
      return pullRequests;
    }
  }
}
