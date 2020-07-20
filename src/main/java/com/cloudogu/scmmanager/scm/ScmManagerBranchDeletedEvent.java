package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import jenkins.scm.api.SCMHead;
import net.sf.json.JSONObject;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

public class ScmManagerBranchDeletedEvent extends ScmManagerHeadEvent {

  private Collection<String> names;

  public ScmManagerBranchDeletedEvent(JSONObject form, Collection<String> names) {
    super(Type.REMOVED, form);
    this.names = names;
  }

  @Override
  Collection<SCMHead> heads(CloneInformation cloneInformation) {
    return names.stream().map(name -> new ScmManagerHead(cloneInformation, name)).collect(toList());
  }
}
