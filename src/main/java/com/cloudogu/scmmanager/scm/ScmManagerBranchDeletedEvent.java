package com.cloudogu.scmmanager.scm;

import net.sf.json.JSONObject;

import java.util.Collection;

public class ScmManagerBranchDeletedEvent extends ScmManagerBranchEvent {

  public ScmManagerBranchDeletedEvent(JSONObject form, Collection<JSONObject> branches) {
    super(Type.REMOVED, form, branches);
  }
}
