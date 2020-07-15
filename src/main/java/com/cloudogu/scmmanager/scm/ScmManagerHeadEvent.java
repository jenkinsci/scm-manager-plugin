package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.scm.SCM;
import jenkins.scm.api.SCMEvent;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.kohsuke.stapler.Stapler;

import java.util.Collections;
import java.util.Map;

public class ScmManagerHeadEvent extends SCMHeadEvent<ScmManagerHeadEvent.TriggerPayload> {

  public ScmManagerHeadEvent() {
    super(Type.UPDATED, new TriggerPayload(), SCMEvent.originOf(Stapler.getCurrentRequest()));
  }

  @Override
  public boolean isMatch(@NonNull SCMNavigator navigator) {
    return false;
  }

  @NonNull
  @Override
  public String getSourceName() {
    return "dummy";
  }

  @NonNull
  @Override
  public Map<SCMHead, SCMRevision> heads(@NonNull SCMSource source) {
    ScmManagerHead head = new ScmManagerHead(new CloneInformation("dummy", "dummy"), "dummy");
    return Collections.singletonMap(head, new ScmManagerRevision(head, "dummy"));
  }

  @Override
  public boolean isMatch(@NonNull SCMSource source) {
    return source instanceof ScmManagerSource && ((ScmManagerSource)source).getRepository().equals("jenkins-plugin/hello-shell/git");
  }

  @Override
  public boolean isMatch(@NonNull SCM scm) {
    return false;
  }

  public static class TriggerPayload {
    String namespace;
    String name;
  }
}
