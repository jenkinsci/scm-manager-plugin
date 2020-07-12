package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;

public class Repository extends HalRepresentation {

  private ApiClient client;

  private String namespace;
  private String name;
  private String type;

//  private CloneInformation cloneInformation;

  public Repository() {
  }

  public Repository(String namespace, String name, String type) {
    this.namespace = namespace;
    this.name = name;
    this.type = type;
  }

  void setClient(ApiClient client) {
    this.client = client;
  }

  public String getType() {
    return type;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return getLinks()
      .getLinkBy("protocol", l -> "http".equals(l.getName()))
      .orElseThrow(() -> new IllegalStateException("no http url"))
      .getHref();
  }

//  public CloneInformation getCloneInformation() {
//    if (cloneInformation == null) {
//      cloneInformation = new CloneInformation(type, getUrl());
//    }
//    return cloneInformation;
//  }


    // TODO Check whether we really have to fetch the revisions here


//  public CompletableFuture<List<Branch>> getBranches() {
//    Optional<Link> branchesLink = getLinks().getLinkBy("branches");
//    if (branchesLink.isPresent()) {
//      return client.get(branchesLink.get().getHref(), "application/vnd.scmm-branchCollection+json;v=2", BranchCollection.class)
//        .thenApply(
//          branchCollection -> branchCollection.get_embedded().getBranches().stream()
//            .peek(b -> b.setCloneInformation(getCloneInformation()))
//            .collect(Collectors.toList())
//        );
//    }
//    return CompletableFuture.completedFuture(Collections.emptyList());
//  }

//  public CompletableFuture<List<Tag>> getTags() {
//    Optional<Link> tagsLink = getLinks().getLinkBy("tags");
//    if (tagsLink.isPresent()) {
//
//      return client.get(tagsLink.get().getHref(), "application/vnd.scmm-tagCollection+json;v=2", TagCollection.class)
//        .thenApply(tags -> tags.get_embedded().getTags().stream()
//          .map(tag -> {
//            Optional<Link> changesetLink = tag.getLinks().getLinkBy("changeset");
//            if (changesetLink.isPresent()) {
//              return client.get(changesetLink.get().getHref(), "application/vnd.scmm-changeset+json;v=2", Changeset.class)
//                .thenApply(changeset -> {
//                  tag.setCloneInformation(getCloneInformation());
//                  tag.setChangeset(changeset);
//                  return tag;
//                });
//            }
//            throw new IllegalStateException("could not find changeset link on tag " + tag.getName());
//          })
//          .collect(Collectors.toList()))
//        .thenCompose(completableFutures -> CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
//          .thenApply(future -> completableFutures.stream().filter(cf -> !cf.isCompletedExceptionally()).map(CompletableFuture::join)
//            .collect(Collectors.toList())));
//    }
//    return CompletableFuture.completedFuture(Collections.emptyList());
//  }

//  public CompletableFuture<List<PullRequest>> getPullRequests() {
//    Optional<Link> pullRequestLink = getLinks().getLinkBy("pullRequest");
//    if (pullRequestLink.isPresent()) {
//      return client.get(pullRequestLink.get().getHref() + "?status=OPEN", "application/vnd.scmm-pullRequest+json;v=2", PullRequestCollection.class)
//        .thenApply(
//          pullRequestCollection -> pullRequestCollection.get_embedded().getPullRequests().stream()
//            .map(pullRequest -> {
//              pullRequest.setCloneInformation(getCloneInformation());
//              // TODO we need the information or the links on the pull request object
//              Optional<Link> branchesLink = getLinks().getLinkBy("branches");
//              if (branchesLink.isPresent()) {
//
//                CompletableFuture<Void> source = client.get(branchesLink.get().getHref() + pullRequest.getSource(), "application/vnd.scmm-branch+json;v=2", Branch.class).thenAccept(pullRequest::setSourceBranch);
//                CompletableFuture<Void> target = client.get(branchesLink.get().getHref() + pullRequest.getTarget(), "application/vnd.scmm-branch+json;v=2", Branch.class).thenAccept(pullRequest::setTargetBranch);
//
//                return CompletableFuture.allOf(source, target).thenApply(v -> pullRequest);
//              }
//              throw new IllegalStateException("could not find branches link on repository");
//            })
//            .collect(Collectors.toList()))
//        .thenCompose(completableFutures -> CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
//          .thenApply(future -> completableFutures.stream().filter(cf -> !cf.isCompletedExceptionally()).map(CompletableFuture::join)
//            .collect(Collectors.toList())));
//    }
//    return CompletableFuture.completedFuture(Collections.emptyList());
//  }

//  private static class BranchCollection {
//    private EmbeddedBranches _embedded;
//
//    public EmbeddedBranches get_embedded() {
//      return _embedded;
//    }
//  }

//  private static class EmbeddedBranches {
//    private List<Branch> branches;
//
//    public List<Branch> getBranches() {
//      return branches;
//    }
//  }

//  private static class TagCollection {
//    private EmbeddedTags _embedded;
//
//    public EmbeddedTags get_embedded() {
//      return _embedded;
//    }
//  }

//  private static class EmbeddedTags {
//    private List<Tag> tags;
//
//    public List<Tag> getTags() {
//      return tags;
//    }
//  }

//  private static class PullRequestCollection {
//    private EmbeddedPullRequests _embedded;
//
//    public EmbeddedPullRequests get_embedded() {
//      return _embedded;
//    }
//  }

//  private static class EmbeddedPullRequests {
//    private List<PullRequest> pullRequests;
//
//    public List<PullRequest> getPullRequests() {
//      return pullRequests;
//    }
//  }

}
