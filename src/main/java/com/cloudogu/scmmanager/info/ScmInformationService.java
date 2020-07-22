package com.cloudogu.scmmanager.info;

import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import jenkins.branch.Branch;
import jenkins.scm.api.SCMHead;
import jenkins.triggers.SCMTriggerItem;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class ScmInformationService {

  public List<JobInformation> resolve(Run<?, ?> run) {
    return resolve(run, run.getParent());
  }

  private List<JobInformation> resolve(Run<?, ?> run, Job<?, ?> job) {
    List<JobInformation> configurations = new ArrayList<>();
    for (JobInformationResolverProvider provider : JobInformationResolverProvider.all()) {
      Optional<JobInformationResolver> resolver = provider.get();
      resolver.ifPresent(jobInformationResolver -> configurations.addAll(jobInformationResolver.resolve(run, job)));
    }
    return configurations;
  }
}
