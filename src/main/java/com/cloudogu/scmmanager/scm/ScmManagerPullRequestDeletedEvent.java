package com.cloudogu.scmmanager.scm;

import net.sf.json.JSONObject;

import java.util.Collection;

public class ScmManagerPullRequestDeletedEvent extends ScmManagerPullRequestEvent {

  public ScmManagerPullRequestDeletedEvent(JSONObject form, Collection<JSONObject> pullRequests) {
    super(Type.REMOVED, form, pullRequests);
  }
}
