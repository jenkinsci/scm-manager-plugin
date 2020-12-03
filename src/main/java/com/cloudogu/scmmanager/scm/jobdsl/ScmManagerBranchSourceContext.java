package com.cloudogu.scmmanager.scm.jobdsl;

import com.cloudogu.scmmanager.scm.BranchDiscoveryTrait;
import com.cloudogu.scmmanager.scm.PullRequestDiscoveryTrait;
import com.cloudogu.scmmanager.scm.TagDiscoveryTrait;
import jenkins.scm.api.trait.SCMSourceTrait;

import java.util.ArrayList;
import java.util.List;

public class ScmManagerBranchSourceContext extends BranchSourceContext {

  private boolean discoverBranches = true;
  private boolean discoverPullRequest = true;
  private boolean discoverTags = false;

  public void discoverBranches(boolean discoverBranches) {
    this.discoverBranches = discoverBranches;
  }

  public void discoverPullRequest(boolean discoverPullRequest) {
    this.discoverPullRequest = discoverPullRequest;
  }

  public void discoverTags(boolean discoverTags) {
    this.discoverTags = discoverTags;
  }

  public List<SCMSourceTrait> getTraits() {
    List<SCMSourceTrait> traits = new ArrayList<>();
    if (discoverBranches) {
      traits.add(new BranchDiscoveryTrait());
    }
    if (discoverPullRequest) {
      traits.add(new PullRequestDiscoveryTrait());
    }
    if (discoverTags) {
      traits.add(new TagDiscoveryTrait());
    }
    return traits;
  }
}
