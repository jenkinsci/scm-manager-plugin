package com.cloudogu.scmmanager.scm;

import net.sf.json.JSONObject;

import java.util.Collection;

public class ScmManagerBranchUpdatedEvent extends ScmManagerBranchEvent {

  public ScmManagerBranchUpdatedEvent(JSONObject form, Collection<JSONObject> branches) {
    super(Type.UPDATED, form, branches);
  }
}
