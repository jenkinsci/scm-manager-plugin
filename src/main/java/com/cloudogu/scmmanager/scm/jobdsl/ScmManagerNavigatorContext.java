package com.cloudogu.scmmanager.scm.jobdsl;

import com.cloudogu.scmmanager.scm.BranchDiscoveryTrait;
import com.cloudogu.scmmanager.scm.PullRequestDiscoveryTrait;
import com.cloudogu.scmmanager.scm.ScmManagerSvnNavigatorTrait;
import com.cloudogu.scmmanager.scm.Subversion;
import com.cloudogu.scmmanager.scm.TagDiscoveryTrait;
import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.DslContext;
import javaposse.jobdsl.dsl.Preconditions;
import jenkins.scm.api.trait.SCMTrait;

import java.util.ArrayList;
import java.util.List;

public class ScmManagerNavigatorContext extends ScmManagerContext {

  private String namespace;

  private boolean discoverBranches = true;
  private boolean discoverPullRequest = true;
  private boolean discoverTags = false;
  private boolean discoverSvn = Subversion.isSupported();

  private String svnIncludes = Subversion.DEFAULT_INCLUDES;
  private String svnExcludes = Subversion.DEFAULT_EXCLUDES;

  private final Executor executor;

  ScmManagerNavigatorContext(Executor executor) {
    this.executor = executor;
  }

  public void namespace(String namespace) {
    this.namespace = namespace;
  }

  public String getNamespace() {
    return namespace;
  }

  public void discoverBranches(boolean discoverBranches) {
    this.discoverBranches = discoverBranches;
  }

  public void discoverPullRequest(boolean discoverPullRequest) {
    this.discoverPullRequest = discoverPullRequest;
  }

  public void discoverTags(boolean discoverTags) {
    this.discoverTags = discoverTags;
  }

  public void discoverSvn(boolean discoverSvn) {
    this.discoverSvn = discoverSvn;
  }

  public void discoverSvn(@DslContext(SubversionContext.class) Runnable closure) {
    this.discoverSvn = true;
    executor.executeInContext(closure, new SubversionContext());
  }

  public List<SCMTrait<? extends SCMTrait<?>>> getTraits() {
    List<SCMTrait<? extends SCMTrait<?>>> traits = new ArrayList<>();
    if (discoverBranches) {
      traits.add(new BranchDiscoveryTrait());
    }
    if (discoverPullRequest) {
      traits.add(new PullRequestDiscoveryTrait());
    }
    if (discoverTags) {
      traits.add(new TagDiscoveryTrait());
    }
    if (discoverSvn) {
      traits.add(new ScmManagerSvnNavigatorTrait(svnIncludes, svnExcludes));
    }
    return traits;
  }

  @Override
  public void validate() {
    super.validate();
    Preconditions.checkNotNullOrEmpty(namespace, "namespace is required");
  }

  public class SubversionContext implements Context {

    public void includes(String includes) {
      svnIncludes = includes;
    }

    public void excludes(String excludes) {
      svnExcludes = excludes;
    }

  }
}
