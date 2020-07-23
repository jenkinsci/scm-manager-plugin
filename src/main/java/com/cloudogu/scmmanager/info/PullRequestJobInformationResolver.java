package com.cloudogu.scmmanager.info;

import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import jenkins.branch.Branch;
import jenkins.scm.api.SCMHead;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.toList;

@Extension(optional = true)
public class PullRequestJobInformationResolver implements JobInformationResolver {
  @Override
  public Collection<JobInformation> resolve(Run<?, ?> run, Job<?, ?> job) {
    if (job == null) {
      return Collections.emptyList();
    }
    BranchJobProperty branchJobProperty = job.getProperty(BranchJobProperty.class);
    if (branchJobProperty == null) {
      return Collections.emptyList();
    }

    return resolve(branchJobProperty);
  }

  private Collection<JobInformation> resolve(BranchJobProperty branchJobProperty) {
    Branch branch = branchJobProperty.getBranch();
    SCM scm = branch.getScm();
    SCMHead scmHead = branch.getHead();
    if (scm instanceof GitSCM && scmHead instanceof ScmManagerPullRequestHead) {
      return ((GitSCM) scm).getUserRemoteConfigs().stream().map(urc ->
        new JobInformation(
          "pr",
          ((ScmManagerPullRequestHead) scmHead).getCloneInformation().getUrl(),
          ((ScmManagerPullRequestHead) scmHead).getId(),
          urc.getCredentialsId()
        )).collect(toList());
    }
    return Collections.emptyList();
  }
}
