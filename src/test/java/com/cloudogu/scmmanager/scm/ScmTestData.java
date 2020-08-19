package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;

final class ScmTestData {

  static final String NAMESPACE = "hitchhiker";
  static final String NAME = "hog";
  static final String TYPE = "git";

  static final Repository REPOSITORY = new Repository(NAMESPACE, NAME, TYPE);

  static final CloneInformation CLONE_INFORMATION = new CloneInformation(
    "git", "https://hitchhiker.com/scm/repo/hitchhiker/hog"
  );


  private ScmTestData() {}


  public static ScmManagerPullRequestHead pullRequest(String id, ScmManagerHead target, ScmManagerHead source) {
    return new ScmManagerPullRequestHead(CLONE_INFORMATION, id, target, source);
  }

  public static ScmManagerPullRequestRevision pullRequestRevision(ScmManagerPullRequestHead pullRequest, String targetRev, String sourceRev) {
    return new ScmManagerPullRequestRevision(
      pullRequest,
      revision(pullRequest.getTarget(), targetRev),
      revision(pullRequest.getSource(), sourceRev)
    );
  }

  public static ScmManagerRevision revision(ScmManagerHead head, String rev) {
    return new ScmManagerRevision(head, rev);
  }

  public static ScmManagerHead branch(String name) {
    return new ScmManagerHead(CLONE_INFORMATION, name);
  }

}
