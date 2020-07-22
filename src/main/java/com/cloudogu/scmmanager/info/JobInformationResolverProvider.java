package com.cloudogu.scmmanager.info;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

import java.util.Optional;

public interface JobInformationResolverProvider extends ExtensionPoint {

  Optional<JobInformationResolver> get();

  static ExtensionList<JobInformationResolverProvider> all() {
    return Jenkins.get().getExtensionList(JobInformationResolverProvider.class);
  }

}
