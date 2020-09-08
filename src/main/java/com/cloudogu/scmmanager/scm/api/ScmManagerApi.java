package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.scm.PluginNotUpToDateException;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jenkins.scm.api.SCMFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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
          .map(prepareTag(repository))
          .collect(Collectors.toList()))
        .thenCompose(completableFutures -> CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
          .thenApply(future -> completableFutures.stream().filter(cf -> !cf.isCompletedExceptionally()).map(CompletableFuture::join)
            .collect(Collectors.toList())));
    }
    return CompletableFuture.completedFuture(emptyList());
  }

  private Function<Tag, CompletableFuture<Tag>> prepareTag(Repository repository) {
    return tag -> {
      Optional<Link> changesetLink = tag.getLinks().getLinkBy("changeset");
      tag.setCloneInformation(repository.getCloneInformation());
      if (tag.getDate() != null) {
        return CompletableFuture.completedFuture(tag);
      } else if (changesetLink.isPresent()) {
        return client.get(changesetLink.get().getHref(), "application/vnd.scmm-changeset+json;v=2", Changeset.class)
          .thenApply(changeset -> {
            tag.setChangeset(changeset);
            return tag;
          });
      }
      throw new IllegalStateException("could not find changeset link on tag " + tag.getName());
    };
  }

  public CompletableFuture<Changeset> getChangeset(Repository repository, String revision) {
    Optional<Link> changesetsLink = repository.getLinks().getLinkBy("changesets");
    if (changesetsLink.isPresent()) {
      return client.get(concat(changesetsLink.get(), revision), "application/vnd.scmm-changeset+json;v=2", Changeset.class);
    }
    throw new IllegalStateException("could not find changesets link on repository " + repository.getName());
  }

  public CompletableFuture<List<PullRequest>> getPullRequests(Repository repository) {
    Optional<Link> pullRequestLink = repository.getLinks().getLinkBy("pullRequest");
    if (pullRequestLink.isPresent()) {
      return client.get(pullRequestLink.get().getHref() + "?status=OPEN", "application/vnd.scmm-pullRequestCollection+json;v=2", PullRequestCollection.class)
        .thenApply(
          pullRequestCollection -> pullRequestCollection.get_embedded().getPullRequests().stream()
            .map(preparePullRequest(repository))
            .collect(Collectors.toList()))
        .thenCompose(completableFutures -> CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
          .thenApply(future -> completableFutures.stream().filter(cf -> !cf.isCompletedExceptionally()).map(CompletableFuture::join)
            .collect(Collectors.toList())));
    }
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  private Function<PullRequest, CompletableFuture<PullRequest>> preparePullRequest(Repository repository) {
    return pullRequest -> {
      pullRequest.setCloneInformation(repository.getCloneInformation());

      CompletableFuture<Void> source = client.get(getPullRequestLink(pullRequest, "sourceBranch"), "application/vnd.scmm-branch+json;v=2", Branch.class).thenAccept(pullRequest::setSourceBranch);
      CompletableFuture<Void> target = client.get(getPullRequestLink(pullRequest, "targetBranch"), "application/vnd.scmm-branch+json;v=2", Branch.class).thenAccept(pullRequest::setTargetBranch);

      return CompletableFuture.allOf(source, target).thenApply(v -> pullRequest);
    };
  }

  private String getPullRequestLink(HalRepresentation hal, String linkName) {
    return hal.getLinks().getLinkBy(linkName)
      .orElseThrow(() -> new PluginNotUpToDateException("could not find link '" + linkName + "', ensure the scm-review-plugin is up-to-date"))
      .getHref();
  }

  public CompletableFuture<Tag> getTag(Repository repository, String tagName) {
    Optional<Link> link = repository.getLinks().getLinkBy("tags");
    return link.map(value -> client.get(concat(value, encode(tagName)), "application/vnd.scmm-tag+json;v=2", Tag.class)
      .thenCompose(prepareTag(repository))).orElse(null);
  }

  public CompletableFuture<PullRequest> getPullRequest(Repository repository, String id) {
    Optional<Link> pullRequestLink = repository.getLinks().getLinkBy("pullRequest");
    return pullRequestLink.map(link -> client.get(concat(link, id), "application/vnd.scmm-pullRequest+json;v=2", PullRequest.class)
      .thenCompose(preparePullRequest(repository))).orElse(null);
  }

  public CompletableFuture<Branch> getBranch(Repository repository, String name) {
    Optional<Link> link = repository.getLinks().getLinkBy("branches");
    return link.map(value -> client.get(concat(value, encode(name)), "application/vnd.scmm-branch+json;v=2", Branch.class)
      .thenApply(branch -> {
        branch.setCloneInformation(repository.getCloneInformation());
        return branch;
      })).orElse(null);
  }

  private String encode(String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("UTF-8 is not supported", e);
    }
  }

  public CompletableFuture<ScmManagerFile> getFileObject(Repository repository, String revision, String path) {
    Optional<Link> sourcesLink = repository.getLinks().getLinkBy("sources");
    if (sourcesLink.isPresent()) {

      return client.get(concat(sourcesLink.get(), revision, path), "application/vnd.scmm-source+json;v=2", FileObject.class)
        .thenApply(fileObject -> new ScmManagerFile(fileObject.getPath(), fileObject.isDirectory() ? SCMFile.Type.DIRECTORY : SCMFile.Type.REGULAR_FILE))
        .exceptionally(ex -> {
          if (ex.getCause() instanceof IllegalReturnStatusException) {
            int statusCode = ((IllegalReturnStatusException) ex.getCause()).getStatusCode();
            if (statusCode == 404) {
              return new ScmManagerFile(path, SCMFile.Type.NONEXISTENT);
            }
          }
          throw new IllegalStateException("failed to get file object", ex);
        });
    }
    throw new IllegalStateException("could not find changesets link on repository " + repository.getName());
  }

  private String concat(Link link, String... suffix) {
    StringBuilder builder = new StringBuilder();
    String href = link.getHref();
    if (href.endsWith("/")) {
      href = href.substring(0, href.length() - 1);
    }
    builder.append(href);
    for (String s : suffix) {
      builder.append("/").append(s);
    }
    return builder.toString();
  }

  private static class FileObject {

    private String path;
    private boolean directory;

    public String getPath() {
      return path;
    }

    public boolean isDirectory() {
      return directory;
    }
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
