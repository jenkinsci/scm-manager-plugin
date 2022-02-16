package com.cloudogu.scmmanager.info;

import com.google.common.base.Strings;
import hudson.Extension;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.Revision;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.util.BuildData;
import hudson.scm.SCM;
import jenkins.plugins.git.GitSCMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Extension(optional = true) // We don't know why, but this is necessary
public class GitScmInformationResolver implements ScmInformationResolver {

  private static final Logger LOG = LoggerFactory.getLogger(GitScmInformationResolver.class);

  private static final String TYPE = "git";

  @Override
  public Collection<JobInformation> resolve(Run<?, ?> run, SCM scm) {
    if (!(scm instanceof GitSCM)) {
      LOG.trace("scm is not of git, skip collecting information");
      return Collections.emptyList();
    }

    GitSCM git = (GitSCM) scm;

    Optional<String> revision = getRevision(run, git);
    if (!revision.isPresent()) {
      LOG.warn("could not extract revision from run, skip collecting information");
      return Collections.emptyList();
    }

    if (!SourceUtil.extractSourceOwner(run).isPresent()) {
      LOG.trace("run does not contain source owner");
      return createInformation(git, revision.get());
    }

    Collection<String> remoteBases = SourceUtil
      .getSources(run, GitSCMSource.class, GitSCMSource::getRemote);

    if (remoteBases.isEmpty()) {
      LOG.warn("source owner has no sources, skip collecting information");
      return Collections.emptyList();
    }

    return createInformation(git, revision.get())
      .stream()
      .filter(jobInformation -> {
        boolean contains = remoteBases.contains(jobInformation.getUrl());
        if (!contains) {
          LOG.trace(
            "skip {}, because it is not part of the source owner {}. Maybe it is a library.",
            jobInformation.getUrl(), remoteBases
          );
        }
        return contains;
      })
      .collect(Collectors.toList());
  }



  private List<JobInformation> createInformation(GitSCM git, String revision) {
    List<JobInformation> information = new ArrayList<>();
    for (UserRemoteConfig urc : git.getUserRemoteConfigs()) {
      information.add(createInformation(urc, revision));
    }
    return information;
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
        } else {
          LOG.trace("revision from build data has no sha1 string");
        }
      } else {
        LOG.trace("build data has no last build revision");
      }
    } else {
      LOG.trace("could not find build data, skip collect information");
    }
    return Optional.empty();
  }
}
