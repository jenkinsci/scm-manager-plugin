package com.cloudogu.scmmanager.info;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

import java.util.Optional;

public interface ScmInformationResolverProvider extends ExtensionPoint {

  Optional<ScmInformationResolver> get();

  static ExtensionList<ScmInformationResolverProvider> all() {
    return Jenkins.get().getExtensionList(ScmInformationResolverProvider.class);
  }

}
