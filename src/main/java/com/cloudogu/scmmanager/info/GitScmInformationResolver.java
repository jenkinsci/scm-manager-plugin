package com.cloudogu.scmmanager.info;

import com.google.common.base.Strings;
import hudson.Extension;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.Revision;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.util.BuildData;
import hudson.scm.SCM;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Extension(optional = true)
public class GitScmInformationResolver implements ScmInformationResolver {

  private static final String TYPE = "git";

  @Override
  public Collection<JobInformation> resolve(Run<?, ? > run, SCM scm) {
    if (!(scm instanceof GitSCM)) {
      return Collections.emptyList();
    }

    GitSCM git = (GitSCM) scm;

    Optional<String> revision = getRevision(run, git);
    if (revision.isPresent()) {
      return createInformation(git, revision.get());
    } else {
      return Collections.emptyList();
    }
  }

  private List<JobInformation> createInformation(GitSCM git, String revision) {
    List<JobInformation> information = new ArrayList<>();
    for (UserRemoteConfig urc : git.getUserRemoteConfigs()) {
      information.add(createInformation(urc, revision));
    }
    return Collections.unmodifiableList(information);
  }

  private JobInformation createInformation(UserRemoteConfig urc, String revision) {
    return new JobInformation(TYPE, urc.getUrl(), revision, urc.getCredentialsId(), false);
  }

  private Optional<String> getRevision(Run<?, ?> run, GitSCM git) {
    BuildData buildData = git.getBuildData(run);
    if (buildData != null) {
      Revision rev = buildData.getLastBuiltRevision();
      if (rev != null) {
        String sha1 = rev.getSha1String();
        if (!Strings.isNullOrEmpty(sha1)) {
          return Optional.of(sha1);
        }
      }
    }
    return Optional.empty();
  }
}
