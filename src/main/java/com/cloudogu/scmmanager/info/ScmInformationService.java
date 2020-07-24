package com.cloudogu.scmmanager.info;

import hudson.model.Job;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
