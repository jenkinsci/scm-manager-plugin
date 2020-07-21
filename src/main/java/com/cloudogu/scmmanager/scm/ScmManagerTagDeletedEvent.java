package com.cloudogu.scmmanager.scm;

import net.sf.json.JSONObject;

import java.util.Collection;

public class ScmManagerTagDeletedEvent extends ScmManagerTagEvent {

  public ScmManagerTagDeletedEvent(JSONObject form, Collection<JSONObject> tags) {
    super(Type.REMOVED, form, tags);
  }
}
