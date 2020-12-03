package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerTag;

public final class ScmTestData {

  static final String NAMESPACE = "hitchhiker";
  static final String NAME = "hog";
  static final String TYPE = "git";

  static final Repository REPOSITORY = new Repository(NAMESPACE, NAME, TYPE);

  static final String URL = "https://hitchhiker.com/scm/repo/hitchhiker/hog";

  static final CloneInformation CLONE_INFORMATION = new CloneInformation(
    TYPE, URL
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

  public static ScmManagerHead tag(String tag) {
    return new ScmManagerTag(CLONE_INFORMATION, tag, 0L);
  }

  public static ScmManagerHead branch(String name) {
    return new ScmManagerHead(CLONE_INFORMATION, name);
  }

  public static SCMBuilderProvider.Context context() {
    return context(TYPE);
  }

  public static SCMBuilderProvider.Context context(String type) {
    CloneInformation cloneInformation = new CloneInformation(type, URL);
    ScmManagerHead develop = new ScmManagerHead(cloneInformation, "develop");
    return new SCMBuilderProvider.Context(
      new LinkBuilder("https://scm-manager.org", NAMESPACE, NAME),
      develop,
      new ScmManagerRevision(develop, "f572d396fae9206628714fb2ce00f72e94f2258f"),
      "creds4scm"
    );
  }

}
