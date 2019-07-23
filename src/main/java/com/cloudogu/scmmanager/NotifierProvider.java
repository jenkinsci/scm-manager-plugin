package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.ScmInformation;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.Optional;

public interface NotifierProvider extends ExtensionPoint {

  @SuppressWarnings("squid:S1452") // we implement this with a concrete Notifier type to simplify unit tests (no casting)
  Optional<? extends Notifier> get(Run<?, ?> run, ScmInformation information) throws IOException;

  static ExtensionList<NotifierProvider> all() {
    return Jenkins.get().getExtensionList(NotifierProvider.class);
  }
}
