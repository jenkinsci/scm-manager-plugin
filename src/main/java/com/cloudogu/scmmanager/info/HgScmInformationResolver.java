package com.cloudogu.scmmanager.info;

import com.google.common.base.Strings;
import hudson.model.Run;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.scm.SCM;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HgScmInformationResolver implements ScmInformationResolver {

  private static final String TYPE = "hg";

  @Override
  public Collection<JobInformation> resolve(Run<?, ?> run , SCM scm) {
    if (!(scm instanceof MercurialSCM)) {
      return Collections.emptyList();
    }

    MercurialSCM hg = (MercurialSCM) scm;

    String source = hg.getSource();
    String revision = getRevision(hg, run);

    if (Strings.isNullOrEmpty(source) || Strings.isNullOrEmpty(revision)) {
      return Collections.emptyList();
    }

    JobInformation config = new JobInformation(TYPE, source, revision, hg.getCredentialsId(), false);
    return Collections.singleton(config);
  }

  private String getRevision(MercurialSCM scm, Run<?, ?> run) {
    Map<String,String> env = new HashMap<>();
    scm.buildEnvironment(run, env);
    return env.get("MERCURIAL_REVISION");
  }
}
