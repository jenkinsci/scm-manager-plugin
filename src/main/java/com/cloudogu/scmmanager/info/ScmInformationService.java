package com.cloudogu.scmmanager.info;

import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.SCM;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScmInformationService {

  /**
   * Use {@link #resolve(Run, SCM)} instead.
   *
   * @param run the build job
   * @return list of resolved job information
   *
   * @deprecated use {@link #resolve(Run, SCM)} instead.
   */
  @Deprecated
  public List<JobInformation> resolve(Run<?, ?> run) {
    Job<?, ?> job = run.getParent();
    return collect(((configurations, resolver) -> configurations.addAll(resolver.resolve(run, job))));
  }

  public List<JobInformation> resolve(Run<?, ?> run, SCM scm) {
    return collect(((configurations, resolver) -> configurations.addAll(resolver.resolve(run, scm))));
  }

  private List<JobInformation> collect(Collector collector) {
    List<JobInformation> configurations = new ArrayList<>();
    for (JobInformationResolverProvider provider : JobInformationResolverProvider.all()) {
      Optional<JobInformationResolver> resolver = provider.get();
      resolver.ifPresent(jobInformationResolver -> collector.collect(configurations, jobInformationResolver));
    }
    return configurations;
  }

  @FunctionalInterface
  public interface Collector {
    void collect(List<JobInformation> configurations, JobInformationResolver resolver);
  }
}
