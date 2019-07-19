package com.cloudogu.scmmanager.info;

import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.SCM;
import jenkins.triggers.SCMTriggerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScmInformationService {

  public List<ScmInformation> resolve(Run<?, ?> run) {
    return resolve(run, run.getParent());
  }

  private List<ScmInformation> resolve(Run<?, ?> run, Job<?, ?> job) {
    List<ScmInformation> configurations = new ArrayList<>();
    SCMTriggerItem trigger = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);
    if (trigger != null) {
      for (SCM scm : trigger.getSCMs()) {
        configurations.addAll(resolve(run, scm));
      }
    }

    return configurations;
  }

  private List<ScmInformation> resolve(Run<?, ?> run, SCM scm) {
    List<ScmInformation> configurations = new ArrayList<>();
    for (ScmInformationResolverProvider provider : ScmInformationResolverProvider.all()) {
      Optional<ScmInformationResolver> resolver = provider.get();
      resolver.ifPresent(scmInformationResolver -> configurations.addAll(scmInformationResolver.resolve(run, scm)));
    }
    return configurations;
  }
}
