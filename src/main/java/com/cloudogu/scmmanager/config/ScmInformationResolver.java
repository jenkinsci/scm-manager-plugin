package com.cloudogu.scmmanager.config;

import hudson.model.Run;
import hudson.scm.SCM;

import java.util.Collection;

public interface ScmInformationResolver {

  Collection<ScmInformation> resolve(Run<?, ?> run, SCM scm);

}
