package com.cloudogu.scmmanager.info;

import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.SCM;

import java.util.Collection;

public interface JobInformationResolver {

  /**
   * Use {@link #resolve(Run, SCM)} instead.
   *
   * @param run the build
   * @param job the job which is currently build
   *
   * @return collection of job information
   * @deprecated use {@link #resolve(Run, SCM)} instead.
   */
  @Deprecated
  Collection<JobInformation> resolve(Run<?, ?> run, Job<?, ?> job);

  Collection<JobInformation> resolve(Run<?, ?> run, SCM scm);
}
