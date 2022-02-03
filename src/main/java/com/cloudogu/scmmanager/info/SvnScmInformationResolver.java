package com.cloudogu.scmmanager.info;

import com.google.common.base.Strings;
import hudson.model.Run;
import hudson.scm.SCM;
import hudson.scm.SubversionSCM;
import jenkins.scm.impl.subversion.SubversionSCMSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SvnScmInformationResolver implements ScmInformationResolver {

  private static final String TYPE = "svn";

  @Override
  public Collection<JobInformation> resolve(Run<?, ?> run, SCM scm) {
    if (!(scm instanceof SubversionSCM)) {
      return Collections.emptyList();
    }

    SubversionSCM svn = (SubversionSCM) scm;

    Map<String, String> env = new HashMap<>();
    svn.buildEnvironment(run, env);

    List<JobInformation> configurations = new ArrayList<>();
    SubversionSCM.ModuleLocation[] locations = svn.getLocations();
    if (locations != null) {
      appendInformation(configurations, locations, env);
    }

    if (!SourceUtil.extractSourceOwner(run).isPresent()) {
      return configurations;
    }

    Collection<String> remoteBases = SourceUtil
      .getSources(run, SubversionSCMSource.class, SubversionSCMSource::getRemoteBase);

    if (remoteBases.isEmpty()) {
      return Collections.emptyList();
    }

    return configurations
      .stream()
      .filter(jobInformation -> remoteBases.stream().anyMatch(remoteBase -> jobInformation.getUrl().startsWith(remoteBase)))
      .collect(Collectors.toList());
  }

  private void appendInformation(List<JobInformation> configurations, SubversionSCM.ModuleLocation[] locations, Map<String, String> env) {
    if (locations.length == 1) {
      appendInformation(configurations, locations[0], env.get("SVN_REVISION"));
    } else if (locations.length > 1) {
      appendMultipleInformation(configurations, locations, env);
    }
  }

  private void appendMultipleInformation(List<JobInformation> configurations, SubversionSCM.ModuleLocation[] locations, Map<String, String> env) {
    for (int i = 0; i < locations.length; i++) {
      appendInformation(configurations, locations[i], env.get("SVN_REVISION_" + (i + 1)));
    }
  }

  private void appendInformation(List<JobInformation> configurations, SubversionSCM.ModuleLocation location, String revision) {
    String url = location.getURL();
    if (!Strings.isNullOrEmpty(url) && !Strings.isNullOrEmpty(revision)) {
      configurations.add(new JobInformation(TYPE, url, revision, location.credentialsId, false));
    }
  }
}
