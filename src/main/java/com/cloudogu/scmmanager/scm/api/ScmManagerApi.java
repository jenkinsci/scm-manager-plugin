package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.scm.api.ApiClient.Promise;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;

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
        .then(
          branchCollection -> branchCollection.get_embedded().getBranches().stream()
            .peek(b -> b.setCloneInformation(repository.getCloneInformation()))
            .collect(Collectors.toList())
        );
    }
    return new Promise<>(CompletableFuture.completedFuture(emptyList()));
  }

  public Promise<List<Tag>> getTags(Repository repository) {
    Optional<Link> tagsLink = repository.getLinks().getLinkBy("tags");
    if (tagsLink.isPresent()) {
      return client.get(tagsLink.get().getHref(), "application/vnd.scmm-tagCollection+json;v=2", TagCollection.class)
        .then(tagCollection -> tagCollection.get_embedded().getTags())
        .then(tags -> tags.stream().peek(t -> t.setCloneInformation(repository.getCloneInformation())).collect(Collectors.toList()));
    }
    return new Promise<>(emptyList());
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
}
