package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ScmManagerApi {

  private final ApiClient client;

  public ScmManagerApi(ApiClient client) {
    this.client = client;
  }

  public ApiClient.Promise<HalRepresentation> index() {
    return client.get("/api/v2", "application/vnd.scmm-index+json;v=2", HalRepresentation.class);
  }

  public ApiClient.Promise<List<Repository>> getRepositories() {
    // TODO pageSize?
    return client.get("/api/v2/repositories?pageSize=2000&sortBy=namespace&sortBy=name", "application/vnd.scmm-repositoryCollection+json;v=2", RepositoryCollection.class)
      .then(collection -> collection.get_embedded().getRepositories());
  }

  public ApiClient.Promise<Repository> getRepository(String namespace, String name) {
    String url = String.format("/api/v2/repositories/%s/%s", namespace, name);
    return client.get(url, "application/vnd.scmm-repository+json;v=2", Repository.class)
      .then(repository -> {
        repository.setClient(client);
        return repository;
      });
  }
  public ApiClient.Promise<List<Branch>> getBranches(Repository repository) {
    Optional<Link> branchesLink = repository.getLinks().getLinkBy("branches");
    if (branchesLink.isPresent()) {
      return client.get(branchesLink.get().getHref(), "application/vnd.scmm-branchCollection+json;v=2", BranchCollection.class)
        .then(
          branchCollection -> branchCollection.get_embedded().getBranches().stream()
            .peek(b -> b.setCloneInformation(repository.getCloneInformation()))
            .collect(Collectors.toList())
        );
    }
    return new ApiClient.Promise<>(CompletableFuture.completedFuture(Collections.emptyList()));
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
}
