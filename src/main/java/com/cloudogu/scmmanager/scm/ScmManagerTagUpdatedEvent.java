package com.cloudogu.scmmanager.scm;

import net.sf.json.JSONObject;

import java.util.Collection;

public class ScmManagerTagUpdatedEvent extends ScmManagerTagEvent {

  public ScmManagerTagUpdatedEvent(JSONObject form, Collection<JSONObject> tags) {
    super(Type.UPDATED, form, tags);
  }
}
