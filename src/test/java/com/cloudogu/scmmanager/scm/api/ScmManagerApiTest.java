package com.cloudogu.scmmanager.scm.api;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ScmManagerApiTest extends ApiClientTestBase {

  @Test
  public void shouldLoadAllRepositories() throws InterruptedException, ExecutionException {
    ScmManagerApi api = new ScmManagerApi(apiClient());

    List<Repository> repositories = api.getRepositories().get();

    assertThat(repositories).hasSize(2);
    assertThat(repositories)
      .extracting("namespace")
      .containsExactly("jenkins-plugin", "plugins");
    assertThat(repositories)
      .extracting("name")
      .containsExactly("hello-shell", "scm-editor-plugin");
  }

  @Test
  public void shouldLoadSingleRepository() throws InterruptedException, ExecutionException {
    ScmManagerApi api = new ScmManagerApi(apiClient());

    Repository repository = api.getRepository("plugins", "scm-editor-plugin").get();

    assertThat(repository.getName()).isEqualTo("scm-editor-plugin");
  }

  @Test
  public void shouldLoadBranches() throws InterruptedException, ExecutionException {
    ScmManagerApi api = new ScmManagerApi(apiClient());

    Repository repository = Mockito.mock(Repository.class);
    when(repository.getLinks()).thenReturn(linkingTo().single(link("branches", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/branches/")).build());
    CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
    when(repository.getCloneInformation()).thenReturn(cloneInformation);

    List<Branch> branches = api.getBranches(repository).get();

    assertThat(branches).hasSize(2);
    assertThat(branches).extracting("name").containsExactly("develop", "master");
    assertThat(branches).extracting("revision").containsExactly("a41666c19c7c868410b80a963a50e8a2a9b0a958", "b41666c19c7c868410b80a963a50e8a2a9b0a958");
    assertThat(branches).extracting("cloneInformation").containsExactly(cloneInformation, cloneInformation);
  }

  @Test
  public void shouldLoadTags() throws InterruptedException, ExecutionException {
    ScmManagerApi api = new ScmManagerApi(apiClient());

    Repository repository = Mockito.mock(Repository.class);
    when(repository.getLinks()).thenReturn(linkingTo().single(link("tags", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/tags/")).build());
    CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
    when(repository.getCloneInformation()).thenReturn(cloneInformation);

    List<Tag> tags = api.getTags(repository).get();

    assertThat(tags).hasSize(1);
    assertThat(tags).extracting("name").containsExactly("1.0.0");
    assertThat(tags).extracting("revision").containsExactly("a41666c19c7c868410b80a963a50e8a2a9b0a958");
    assertThat(tags).extracting("changeset").extracting("id").containsExactly("a41666c19c7c868410b80a963a50e8a2a9b0a958");
    assertThat(tags).extracting("cloneInformation").containsExactly(cloneInformation);
  }

  @Test
  public void shouldLoadSingleTag() throws ExecutionException, InterruptedException {
    ScmManagerApi api = new ScmManagerApi(apiClient());

    Repository repository = Mockito.mock(Repository.class);
    when(repository.getLinks()).thenReturn(linkingTo().single(link("tags", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/tags/")).build());
    CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
    when(repository.getCloneInformation()).thenReturn(cloneInformation);

    Tag tag = api.getTag(repository, "1.0.0").get();
    assertThat(tag.getName()).isEqualTo("1.0.0");
    assertThat(tag.getRevision()).isEqualTo("a41666c19c7c868410b80a963a50e8a2a9b0a958");
  }

  @Test
  public void shouldLoadSingleBranch() throws ExecutionException, InterruptedException {
    ScmManagerApi api = new ScmManagerApi(apiClient());

    Repository repository = Mockito.mock(Repository.class);
    when(repository.getLinks()).thenReturn(linkingTo().single(link("branches", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/branches/")).build());
    CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
    when(repository.getCloneInformation()).thenReturn(cloneInformation);

    Branch branch = api.getBranch(repository, "develop").get();
    assertThat(branch.getName()).isEqualTo("develop");
    assertThat(branch.getRevision()).isEqualTo("6f6ea59a8c504f2c9f3cd93c02e289aa8b65e9c3");
  }

  @Test
  public void shouldLoadPullRequests() throws ExecutionException, InterruptedException {
    ScmManagerApi api = new ScmManagerApi(apiClient());

    Repository repository = Mockito.mock(Repository.class);
    when(repository.getLinks()).thenReturn(linkingTo().single(link("pullRequest", "/scm/api/v2/pull-requests/jenkins-plugin/hello-shell")).build());
    CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
    when(repository.getCloneInformation()).thenReturn(cloneInformation);

    List<PullRequest> pullRequests = api.getPullRequests(repository).get();
    assertThat(pullRequests).hasSize(1);

    PullRequest pullRequest = pullRequests.get(0);
    assertThat(pullRequest.getId()).isEqualTo("1");
    assertThat(pullRequest.getSource()).isEqualTo("develop");
    assertThat(pullRequest.getTarget()).isEqualTo("master");
  }

  @Test
  public void shouldLoadSinglePullRequest() throws ExecutionException, InterruptedException {
    ScmManagerApi api = new ScmManagerApi(apiClient());

    Repository repository = Mockito.mock(Repository.class);
    when(repository.getLinks()).thenReturn(linkingTo().single(link("pullRequest", "/scm/api/v2/pull-requests/jenkins-plugin/hello-shell")).build());
    CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
    when(repository.getCloneInformation()).thenReturn(cloneInformation);

    PullRequest pullRequest = api.getPullRequest(repository, "1").get();
    assertThat(pullRequest.getId()).isEqualTo("1");
    assertThat(pullRequest.getSource()).isEqualTo("develop");
    assertThat(pullRequest.getTarget()).isEqualTo("master");
  }
}
