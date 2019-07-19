package com.cloudogu.scmmanager.info;

import jenkins.model.Jenkins;

import java.util.Optional;

abstract class AbstractScmInformationResolverProvider implements ScmInformationResolverProvider {

  private final String plugin;

  AbstractScmInformationResolverProvider(String plugin) {
    this.plugin = plugin;
  }

  @Override
  public Optional<ScmInformationResolver> get() {
    if (Jenkins.get().getPlugin(plugin) != null) {
      return Optional.of(create());
    }
    return Optional.empty();
  }

  public abstract ScmInformationResolver create();
}
