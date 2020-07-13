package com.cloudogu.scmmanager.scm.api;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ScmManagerApiTest extends ApiClientTestBase {

  @Test
  public void shouldLoadAllRepositories() throws InterruptedException {
    ScmManagerApi api = new ScmManagerApi(apiClient());

    List<Repository> repositories = api.getRepositories()
      .orElseThrow(error -> new RuntimeException("unexpected error: " + error.getMessage()));

    assertThat(repositories).hasSize(2);
    assertThat(repositories)
      .extracting("namespace")
      .containsExactly("jenkins-plugin", "plugins");
    assertThat(repositories)
      .extracting("name")
      .containsExactly("hello-shell", "scm-editor-plugin");
  }

  @Test
  public void shouldLoadSingleRepository() throws InterruptedException {
    ScmManagerApi api = new ScmManagerApi(apiClient());

    Repository repository = api.getRepository("plugins", "scm-editor-plugin")
      .orElseThrow(error -> new RuntimeException("unexpected error: " + error.getMessage()));

    assertThat(repository.getName()).isEqualTo("scm-editor-plugin");
  }
}
