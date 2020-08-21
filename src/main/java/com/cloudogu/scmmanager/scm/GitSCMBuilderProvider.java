package com.cloudogu.scmmanager.scm;

import hudson.Extension;
import jenkins.scm.api.trait.SCMBuilder;

@Extension(optional = true)
public class GitSCMBuilderProvider extends SCMBuilderProvider {

  private static final String TYPE = "git";

  public GitSCMBuilderProvider() {
    super(TYPE, ScmManagerGitSCMBuilder.class);
  }

  @Override
  protected SCMBuilder<?, ?> create(Context context) {
    return new ScmManagerGitSCMBuilder(
      context.getLinkBuilder(),
      context.getHead(),
      context.getRevision(),
      context.getCredentialsId()
    );
  }
}
