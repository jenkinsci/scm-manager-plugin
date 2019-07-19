package com.cloudogu.scmmanager.info;

import com.google.common.base.Strings;
import hudson.model.Run;
import hudson.scm.SCM;
import hudson.scm.SubversionSCM;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvnScmInformationResolver implements ScmInformationResolver {

  private static final String TYPE = "svn";

  @Override
  public Collection<ScmInformation> resolve(Run<?, ?> run, SCM scm) {
    if (!(scm instanceof SubversionSCM)) {
      return Collections.emptyList();
    }

    SubversionSCM svn = (SubversionSCM) scm;

    Map<String, String> env = new HashMap<>();
    svn.buildEnvironment(run, env);

    List<ScmInformation> configurations = new ArrayList<>();
    SubversionSCM.ModuleLocation[] locations = svn.getLocations();
    if (locations != null) {
      appendInformation(configurations, locations, env);
    }
    return Collections.unmodifiableList(configurations);
  }

  private void appendInformation(List<ScmInformation> configurations, SubversionSCM.ModuleLocation[] locations, Map<String, String> env) {
    if (locations.length == 1) {
      appendInformation(configurations, locations[0], env.get("SVN_REVISION"));
    } else if (locations.length > 1) {
      appendMultipleInformation(configurations, locations, env);
    }
  }

  private void appendMultipleInformation(List<ScmInformation> configurations, SubversionSCM.ModuleLocation[] locations, Map<String, String> env) {
    for (int i = 0; i < locations.length; i++) {
      appendInformation(configurations, locations[i], env.get("SVN_REVISION_" + (i + 1)));
    }
  }

  private void appendInformation(List<ScmInformation> configurations, SubversionSCM.ModuleLocation location, String revision) {
    String url = location.getURL();
    if (!Strings.isNullOrEmpty(url) && !Strings.isNullOrEmpty(revision)) {
      configurations.add(new ScmInformation(TYPE, url, revision, location.credentialsId));
    }
  }
}
