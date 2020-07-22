package com.cloudogu.scmmanager.info;

import hudson.model.Job;
import hudson.model.Run;

import java.util.Collection;

public interface JobInformationResolver {

  Collection<JobInformation> resolve(Run<?, ?> run, Job<?, ?> job);

}
