package com.cloudogu.scmmanager.config;

import hudson.Extension;

@Extension(optional = true)
public class HgScmInformationResolverProvider extends AbstractScmInformationResolverProvider {

  public HgScmInformationResolverProvider() {
    super("mercurial");
  }

  @Override
  public ScmInformationResolver create() {
    return new HgScmInformationResolver();
  }
}
