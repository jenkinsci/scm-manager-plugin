package com.cloudogu.scmmanager.scm.api;

import jenkins.scm.api.SCMFile;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ScmManagerApiTest extends ApiClientTestBase {

    @Test
    public void shouldLoadAllNamespaces() throws InterruptedException, ExecutionException {
        ScmManagerApi api = new ScmManagerApi(apiClient());

        List<Namespace> namespaces = api.getNamespaces().get();

        assertThat(namespaces).hasSize(2);
        assertThat(namespaces)
            .extracting("namespace")
            .containsExactly("hitchhiker", "guide");
    }

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
    public void shouldLoadAllRepositoriesForSingleNamespace() throws InterruptedException, ExecutionException {
        ScmManagerApi api = new ScmManagerApi(apiClient());

        Namespace namespace = new Namespace(
            linkingTo().single(link("repositories", "/api/v2/repositories/hitchhiker")).build(),
            "hitchhiker");
        List<Repository> repositories = api.getRepositories(namespace).get();

        assertThat(repositories).hasSize(2);
        assertThat(repositories)
            .extracting("namespace")
            .containsExactly("hitchhiker", "hitchhiker");
        assertThat(repositories)
            .extracting("name")
            .containsExactly("hello-shell", "scm-editor-plugin");
    }

    @Test
    public void shouldLoadAllRepositoriesForSingleNamespaceAsString() throws InterruptedException, ExecutionException {
        ScmManagerApi api = new ScmManagerApi(apiClient());

        List<Repository> repositories = api.getRepositories("hitchhiker").get();

        assertThat(repositories).hasSize(2);
        assertThat(repositories)
            .extracting("namespace")
            .containsExactly("hitchhiker", "hitchhiker");
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
        when(repository.getCloneInformation("http")).thenReturn(cloneInformation);

        List<Branch> branches = api.getBranches(repository).get();

        assertThat(branches).hasSize(2);
        assertThat(branches).extracting("name").containsExactly("develop", "master");
        assertThat(branches).extracting("revision").containsExactly("a41666c19c7c868410b80a963a50e8a2a9b0a958", "b41666c19c7c868410b80a963a50e8a2a9b0a958");
        assertThat(branches).extracting("cloneInformation").containsExactly(cloneInformation, cloneInformation);
    }

    @Test
    public void shouldLoadTagsWithChangesetsIfDateMissing() throws InterruptedException, ExecutionException {
        ScmManagerApi api = new ScmManagerApi(apiClient());

        Repository repository = Mockito.mock(Repository.class);
        when(repository.getLinks()).thenReturn(linkingTo().single(link("tags", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/tags/")).build());
        CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
        when(repository.getCloneInformation("http")).thenReturn(cloneInformation);

        List<Tag> tags = api.getTags(repository).get();

        assertThat(tags).hasSize(1);
        Tag tag = tags.get(0);
        assertThat(tag.getName()).isEqualTo("1.0.0");
        assertThat(tag.getRevision()).isEqualTo("a41666c19c7c868410b80a963a50e8a2a9b0a958");
        assertThat(tag.getDate()).isEqualTo("2020-06-22T11:57:28Z");
        assertThat(tag.getCloneInformation()).isEqualTo(cloneInformation);
    }

    @Test
    public void shouldLoadTagsWithoutChangesetsWhenTagHasDate() throws InterruptedException, ExecutionException {
        ScmManagerApi api = new ScmManagerApi(apiClient());

        Repository repository = Mockito.mock(Repository.class);
        when(repository.getLinks()).thenReturn(linkingTo().single(link("tags", "/scm/api/v2/repositories/jenkins-plugin/hello-shell-with-date/tags/")).build());
        CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
        when(repository.getCloneInformation("http")).thenReturn(cloneInformation);

        List<Tag> tags = api.getTags(repository).get();

        assertThat(tags).hasSize(1);
        Tag tag = tags.get(0);
        assertThat(tag.getName()).isEqualTo("1.0.0");
        assertThat(tag.getRevision()).isEqualTo("a41666c19c7c868410b80a963a50e8a2a9b0a958");
        assertThat(tag.getDate()).isEqualTo("2020-06-27T09:46:38Z");
        assertThat(tag.getCloneInformation()).isEqualTo(cloneInformation);
    }

    @Test
    public void shouldLoadSingleTag() throws ExecutionException, InterruptedException {
        ScmManagerApi api = new ScmManagerApi(apiClient());

        Repository repository = Mockito.mock(Repository.class);
        when(repository.getLinks()).thenReturn(linkingTo().single(link("tags", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/tags/")).build());
        CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
        when(repository.getCloneInformation("http")).thenReturn(cloneInformation);

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
        when(repository.getCloneInformation("http")).thenReturn(cloneInformation);

        Branch branch = api.getBranch(repository, "develop").get();
        assertThat(branch.getName()).isEqualTo("develop");
        assertThat(branch.getRevision()).isEqualTo("6f6ea59a8c504f2c9f3cd93c02e289aa8b65e9c3");
    }

    @Test
    public void shouldLoadSingleBranchWithSlashInName() throws ExecutionException, InterruptedException {
        ScmManagerApi api = new ScmManagerApi(apiClient());

        Repository repository = Mockito.mock(Repository.class);
        when(repository.getLinks()).thenReturn(linkingTo().single(link("branches", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/branches/")).build());
        CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
        when(repository.getCloneInformation("http")).thenReturn(cloneInformation);

        Branch branch = api.getBranch(repository, "feature/readme").get();
        assertThat(branch.getName()).isEqualTo("feature/readme");
        assertThat(branch.getRevision()).isEqualTo("1d56c86c55170e71e16bc3bba82bb28e9328ce47");
    }

    @Test
    public void shouldLoadPullRequests() throws ExecutionException, InterruptedException {
        ScmManagerApi api = new ScmManagerApi(apiClient());

        Repository repository = Mockito.mock(Repository.class);
        when(repository.getLinks()).thenReturn(linkingTo().single(link("pullRequest", "/scm/api/v2/pull-requests/jenkins-plugin/hello-shell")).build());
        CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
        when(repository.getCloneInformation("http")).thenReturn(cloneInformation);

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
        when(repository.getCloneInformation("http")).thenReturn(cloneInformation);

        PullRequest pullRequest = api.getPullRequest(repository, "1").get();
        assertThat(pullRequest.getId()).isEqualTo("1");
        assertThat(pullRequest.getSource()).isEqualTo("develop");
        assertThat(pullRequest.getTarget()).isEqualTo("master");
    }

    @Test
    public void shouldLoadSingleChangeset() throws ExecutionException, InterruptedException {
        ScmManagerApi api = new ScmManagerApi(apiClient());

        Repository repository = Mockito.mock(Repository.class);
        when(repository.getLinks()).thenReturn(linkingTo().single(link("changesets", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/changesets/")).build());
        CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
        when(repository.getCloneInformation("http")).thenReturn(cloneInformation);

        Changeset changeset = api.getChangeset(repository, "a41666c19c7c868410b80a963a50e8a2a9b0a958").get();
        assertThat(changeset.getId()).isEqualTo("a41666c19c7c868410b80a963a50e8a2a9b0a958");
        assertThat(changeset.getDate()).isEqualTo("2020-06-22T11:57:28Z");
    }

    @Test
    public void shouldLoadSingleFile() throws ExecutionException, InterruptedException {
        ScmManagerApi api = new ScmManagerApi(apiClient());

        Repository repository = Mockito.mock(Repository.class);
        when(repository.getLinks()).thenReturn(linkingTo().single(link("sources", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/sources/")).build());
        CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
        when(repository.getCloneInformation("http")).thenReturn(cloneInformation);

        ScmManagerFile fo = api.getFileObject(repository, "a41666c19c7c868410b80a963a50e8a2a9b0a958", "Jenkinsfile").get();
        assertThat(fo.getPath()).isEqualTo("Jenkinsfile");
        assertThat(fo.getType()).isEqualTo(SCMFile.Type.REGULAR_FILE);
    }

    @Test
    public void shouldLoadSingleNonExistingFile() throws ExecutionException, InterruptedException {
        ScmManagerApi api = new ScmManagerApi(apiClient());

        Repository repository = Mockito.mock(Repository.class);
        when(repository.getLinks()).thenReturn(linkingTo().single(link("sources", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/sources/")).build());
        CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
        when(repository.getCloneInformation("http")).thenReturn(cloneInformation);

        ScmManagerFile fo = api.getFileObject(repository, "a41666c19c7c868410b80a963a50e8a2a9b0a958", "FileDeJenkins").get();
        assertThat(fo.getPath()).isEqualTo("FileDeJenkins");
        assertThat(fo.getType()).isEqualTo(SCMFile.Type.NONEXISTENT);
    }

    @Test
    public void shouldLoadSingleDirectory() throws ExecutionException, InterruptedException {
        ScmManagerApi api = new ScmManagerApi(apiClient());

        Repository repository = Mockito.mock(Repository.class);
        when(repository.getLinks()).thenReturn(linkingTo().single(link("sources", "/scm/api/v2/repositories/jenkins-plugin/hello-shell/sources/")).build());
        CloneInformation cloneInformation = new CloneInformation("git", "http://hitchhiker.com/");
        when(repository.getCloneInformation("http")).thenReturn(cloneInformation);

        ScmManagerFile fo = api.getFileObject(repository, "42a84101678bf08ff0f33556cf88db48e248587c", "src").get();
        assertThat(fo.getPath()).isEqualTo("src");
        assertThat(fo.getType()).isEqualTo(SCMFile.Type.DIRECTORY);
    }
}
