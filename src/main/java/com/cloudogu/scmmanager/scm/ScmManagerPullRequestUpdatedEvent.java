package com.cloudogu.scmmanager.scm;

import net.sf.json.JSONObject;

import java.util.Collection;

public class ScmManagerPullRequestUpdatedEvent extends ScmManagerPullRequestEvent {

  public ScmManagerPullRequestUpdatedEvent(JSONObject form, Collection<JSONObject> pullRequests) {
    super(Type.CREATED, form, pullRequests);
  }
}
