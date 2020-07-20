package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerTag;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.TagSCMHead;
import net.sf.json.JSONObject;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

public class ScmManagerTagUpdatedEvent extends ScmManagerHeadEvent {
  private Collection<String> names;

  public ScmManagerTagUpdatedEvent(JSONObject form, Collection<String> names) {
    super(Type.UPDATED, form);
    this.names = names;
  }

  @Override
  Collection<SCMHead> heads(CloneInformation cloneInformation) {
    return names.stream().map(name -> new ScmManagerTag(cloneInformation, name, System.currentTimeMillis())).collect(toList());
  }
}
