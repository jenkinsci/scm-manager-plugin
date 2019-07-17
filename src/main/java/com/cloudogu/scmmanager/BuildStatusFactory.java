package com.cloudogu.scmmanager;

import hudson.model.Result;
import hudson.model.Run;

final class BuildStatusFactory {

  private BuildStatusFactory() {
  }

  public BuildStatus create(String rootUrl, Run<?, ?> run, Result result) {
    String displayName = run.getParent().getFullName();
    String url = rootUrl + run.getUrl();

    if (result == null) {
      return BuildStatus.pending(displayName, url);
    } else if (result == Result.SUCCESS) {
      return BuildStatus.success(displayName, url);
    } else if (result == Result.FAILURE) {
      return BuildStatus.failure(displayName, url);
    } else if (result == Result.ABORTED) {
      return BuildStatus.aborted(displayName, url);
    } else {
      return null;
    }
  }

}
