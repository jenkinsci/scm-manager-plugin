package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import jenkins.scm.api.SCMHead;
import net.sf.json.JSONObject;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

abstract class ScmManagerBranchEvent extends ScmManagerHeadEvent {

  private Collection<String> names;

  ScmManagerBranchEvent(Type type, JSONObject form, Collection<JSONObject> branches) {
    super(type, form);
    this.names = branches.stream().map(branch -> branch.getString("name")).collect(toList());
  }

  @Override
  Collection<SCMHead> heads(CloneInformation cloneInformation) {
    return names.stream().map(name -> new ScmManagerHead(cloneInformation, name)).collect(toList());
  }
}
