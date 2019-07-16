package com.cloudogu.scmmanager.config;

import hudson.Extension;

@Extension(optional = true)
public class GitScmInformationResolverProvider extends AbstractScmInformationResolverProvider {

  public GitScmInformationResolverProvider() {
    super("git");
  }

  @Override
  public ScmInformationResolver create() {
    return new GitScmInformationResolver();
  }
}
