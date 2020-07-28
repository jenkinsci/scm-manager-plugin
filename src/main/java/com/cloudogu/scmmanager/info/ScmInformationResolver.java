package com.cloudogu.scmmanager.info;

import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.SCM;
import jenkins.triggers.SCMTriggerItem;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public interface ScmInformationResolver extends JobInformationResolver {

  Collection<JobInformation> resolve(Run<?, ?> run, SCM scm);

  @Override
  default Collection<JobInformation> resolve(Run<?, ?> run, Job<?, ?> job) {
    SCMTriggerItem trigger = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);
    if (trigger != null) {
      return trigger.getSCMs().stream().flatMap(scm -> resolve(run, scm).stream()).collect(toList());
    }
    return emptyList();
  }
}
