package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.config.ScmInformation;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.Optional;

public interface NotifierProvider extends ExtensionPoint {

  @SuppressWarnings("squid:S1452")
  Optional<? extends Notifier> get(Run<?, ?> run, ScmInformation information) throws IOException;

  static ExtensionList<NotifierProvider> all() {
    return Jenkins.get().getExtensionList(NotifierProvider.class);
  }
}
