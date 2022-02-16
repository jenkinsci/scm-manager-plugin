package com.cloudogu.scmmanager.info;

import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import jenkins.branch.Branch;
import jenkins.scm.api.SCMHead;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.toList;

public class PullRequestJobInformationResolver implements JobInformationResolver {

  private static final Logger LOG = LoggerFactory.getLogger(PullRequestJobInformationResolver.class);

  public static final String TYPE = "pr";

  @Override
  public Collection<JobInformation> resolve(Run<?, ?> run, SCM scm) {
    return resolve(run, run.getParent());
  }

  @Override
  public Collection<JobInformation> resolve(Run<?, ?> run, Job<?, ?> job) {
    if (job == null) {
      LOG.trace("job is null, skip collecting information");
      return Collections.emptyList();
    }
    BranchJobProperty branchJobProperty = job.getProperty(BranchJobProperty.class);
    if (branchJobProperty == null) {
      LOG.trace("job does not contain a branch job property, skip collecting information");
      return Collections.emptyList();
    }

    return resolve(branchJobProperty);
  }

  private Collection<JobInformation> resolve(BranchJobProperty branchJobProperty) {
    Branch branch = branchJobProperty.getBranch();

    SCM scm = branch.getScm();
    if (!(scm instanceof GitSCM)) {
      LOG.trace("scm is not a git scm, skip collecting information");
      return Collections.emptyList();
    }

    SCMHead scmHead = branch.getHead();
    if (!(scmHead instanceof ScmManagerPullRequestHead)) {
      LOG.trace("head is not a pull request head, skip collecting information");
      return Collections.emptyList();
    }

    return ((GitSCM) scm).getUserRemoteConfigs().stream().map(urc ->
      new JobInformation(
        TYPE,
        ((ScmManagerPullRequestHead) scmHead).getCloneInformation().getUrl(),
        ((ScmManagerPullRequestHead) scmHead).getId(),
        urc.getCredentialsId(),
        true
      )).collect(toList());
  }
}
