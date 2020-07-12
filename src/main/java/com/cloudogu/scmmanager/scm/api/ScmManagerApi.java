package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;

import java.util.List;

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

}
