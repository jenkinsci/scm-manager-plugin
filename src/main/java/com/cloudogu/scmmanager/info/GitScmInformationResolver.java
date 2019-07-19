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
  public Collection<ScmInformation> resolve(Run<?, ? > run, SCM scm) {
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

  private List<ScmInformation> createInformation(GitSCM git, String revision) {
    List<ScmInformation> information = new ArrayList<>();
    for (UserRemoteConfig urc : git.getUserRemoteConfigs()) {
      information.add(createInformation(urc, revision));
    }
    return Collections.unmodifiableList(information);
  }

  private ScmInformation createInformation(UserRemoteConfig urc, String revision) {
    return new ScmInformation(TYPE, urc.getUrl(), revision, urc.getCredentialsId());
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
