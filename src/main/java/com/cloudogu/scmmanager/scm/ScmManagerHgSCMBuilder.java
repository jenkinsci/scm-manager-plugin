package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.plugins.mercurial.MercurialSCMBuilder;
import hudson.plugins.mercurial.MercurialSCMSource;
import jenkins.scm.api.SCMRevision;

public class ScmManagerHgSCMBuilder extends MercurialSCMBuilder<ScmManagerHgSCMBuilder> {
  public ScmManagerHgSCMBuilder(@NonNull ScmManagerHead head, SCMRevision revision, String credentialsId) {
    super(head, revision, head.getCloneInformation().getUrl(), credentialsId);
    if (revision instanceof ScmManagerRevision) {
      withRevision(new MercurialSCMSource.MercurialRevision(head, ((ScmManagerRevision) revision).getRevision()));
    }
  }
}
