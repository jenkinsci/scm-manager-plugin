package com.cloudogu.scmmanager.info;

import com.google.common.base.Strings;
import hudson.model.Run;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.plugins.mercurial.MercurialSCMSource;
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

    String revision = getRevision(hg, run);
    String source = hg.getSource();
    if (Strings.isNullOrEmpty(source) || Strings.isNullOrEmpty(revision)) {
      return Collections.emptyList();
    }

    if (!SourceUtil.extractSourceOwner(run).isPresent()) {
      return Collections.singleton(createInformation(hg, revision, source));
    }

    Collection<String> remoteBases = SourceUtil
      .getSources(run, MercurialSCMSource.class, MercurialSCMSource::getSource);

    if (remoteBases.isEmpty()) {
      return Collections.emptyList();
    }

    JobInformation config = createInformation(hg, revision, source);

    if (remoteBases.contains(config.getUrl())) {
      return Collections.singleton(config);
    }

    return Collections.emptyList();
  }

  private JobInformation createInformation(MercurialSCM hg, String revision, String source) {
    return new JobInformation(TYPE, source, revision, hg.getCredentialsId(), false);
  }

  private String getRevision(MercurialSCM scm, Run<?, ?> run) {
    Map<String,String> env = new HashMap<>();
    scm.buildEnvironment(run, env);
    return env.get("MERCURIAL_REVISION");
  }
}
