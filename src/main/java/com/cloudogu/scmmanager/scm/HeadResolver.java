package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.Branch;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerTag;
import com.cloudogu.scmmanager.scm.api.Tag;
import jenkins.scm.api.SCMHead;

import java.util.concurrent.CompletableFuture;

public class HeadResolver {

  private final ScmManagerApi api;
  private final Repository repository;

  HeadResolver(ScmManagerApi api, Repository repository) {
    this.api = api;
    this.repository = repository;
  }

  CompletableFuture<ScmManagerRevision> resolve(SCMHead head) {
    if (head instanceof ScmManagerTag) {
      return api.getTag(repository, head.getName()).thenApply(Tag::revision);
    } else if (head instanceof ScmManagerPullRequestHead) {
      String branch = ((ScmManagerPullRequestHead) head).getSource().getName();
      return api.getBranch(repository, branch).thenApply(Branch::revision);
    } else if (head instanceof ScmManagerHead) {
      return api.getBranch(repository, head.getName()).thenApply(Branch::revision);
    } else {
      throw new IllegalArgumentException(head.getName() + " in not an instance of " + ScmManagerHead.class.getName());
    }
  }
}
