package com.cloudogu.scmmanager;

import hudson.model.Result;
import hudson.model.Run;

class BuildStatusFactory {

    BuildStatusFactory() {
    }

    public BuildStatus create(String rootUrl, Run<?, ?> run, Result result) {
        String name = run.getParent().getFullName();
        String displayName = run.getParent().getFullDisplayName();
        String url = rootUrl + run.getUrl();

        if (result == null) {
            return BuildStatus.pending(name, displayName, url);
        } else if (result == Result.SUCCESS) {
            return BuildStatus.success(name, displayName, url);
        } else if (result == Result.FAILURE) {
            return BuildStatus.failure(name, displayName, url);
        } else if (result == Result.UNSTABLE) {
            return BuildStatus.unstable(name, displayName, url);
        } else if (result == Result.ABORTED) {
            return BuildStatus.aborted(name, displayName, url);
        } else {
            return null;
        }
    }

}
