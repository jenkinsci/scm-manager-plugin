package com.cloudogu.scmmanager.scm.api;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

  @Test
  public void shouldLoadBranches() throws InterruptedException {
    ScmManagerApi api = new ScmManagerApi(apiClient());

    Repository repository = Mockito.mock(Repository.class);
    when(repository.getLinks()).thenReturn(linkingTo().single(link("branches", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/branches/")).build());
    CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
    when(repository.getCloneInformation()).thenReturn(cloneInformation);

    List<Branch> branches = api.getBranches(repository).orElseThrow(e -> new RuntimeException(e.getMessage()));

    assertThat(branches).hasSize(2);
    assertThat(branches).extracting("name").containsExactly("develop", "master");
    assertThat(branches).extracting("revision").containsExactly("a41666c19c7c868410b80a963a50e8a2a9b0a958", "b41666c19c7c868410b80a963a50e8a2a9b0a958");
    assertThat(branches).extracting("cloneInformation").containsExactly(cloneInformation, cloneInformation);
  }

  @Test
  public void shouldLoadTags() throws InterruptedException {
    ScmManagerApi api = new ScmManagerApi(apiClient());

    Repository repository = Mockito.mock(Repository.class);
    when(repository.getLinks()).thenReturn(linkingTo().single(link("tags", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/tags/")).build());
    CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
    when(repository.getCloneInformation()).thenReturn(cloneInformation);

    List<Tag> tags = api.getTags(repository).orElseThrow(e -> new RuntimeException(e.getMessage()));

    assertThat(tags).hasSize(2);
    assertThat(tags).extracting("name").containsExactly("develop", "master");
    assertThat(tags).extracting("revision").containsExactly("a41666c19c7c868410b80a963a50e8a2a9b0a958", "b41666c19c7c868410b80a963a50e8a2a9b0a958");
    assertThat(tags).extracting("cloneInformation").containsExactly(cloneInformation, cloneInformation);
  }
}
