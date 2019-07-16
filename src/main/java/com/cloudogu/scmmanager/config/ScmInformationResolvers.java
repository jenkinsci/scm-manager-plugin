package com.cloudogu.scmmanager.config;

import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.SCM;
import jenkins.triggers.SCMTriggerItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ScmInformationResolvers {

  private static final Logger LOG = LoggerFactory.getLogger(ScmInformationResolvers.class);

  private ScmInformationResolvers() {
  }

  public static List<ScmInformation> resolve(Run<?, ?> run) {
    try {
      return resolve(run, run.getParent());
    } catch (IOException ex) {
      LOG.warn("failed to resolve scm configuration from run", ex);
      return Collections.emptyList();
    }
  }

  private static List<ScmInformation> resolve(Run<?, ?> run, Job<?, ?> job) throws IOException {
    List<ScmInformation> configurations = new ArrayList<>();
    SCMTriggerItem trigger = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);
    if (trigger != null) {
      for (SCM scm : trigger.getSCMs()) {
        configurations.addAll(resolve(run, scm));
      }
    }

    return configurations;
  }

  private static List<ScmInformation> resolve(Run<?, ?> run, SCM scm) throws IOException {
    List<ScmInformation> configurations = new ArrayList<>();
    for (ScmInformationResolverProvider provider : ScmInformationResolverProvider.all()) {
      Optional<ScmInformationResolver> resolver = provider.get();
      if (resolver.isPresent()) {
        configurations.addAll(resolver.get().resolve(run, scm));
      }
    }
    return configurations;
  }
}
