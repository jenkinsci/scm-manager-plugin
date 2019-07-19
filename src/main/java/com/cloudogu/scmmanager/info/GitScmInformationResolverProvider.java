package com.cloudogu.scmmanager.info;

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
