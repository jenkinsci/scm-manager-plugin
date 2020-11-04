package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceEvent;
import net.sf.json.JSONObject;

public abstract class ScmManagerSourceEvent extends SCMSourceEvent<ScmManagerSourceEvent.Payload> {

  private final String serverUrl;

  public static ScmManagerSourceEvent from(JSONObject form) {
    if (form.containsKey("namespace") && form.containsKey("name")) {
      return new ScmManagerSingleSourceEvent(form);
    } else {
      return new ScmManagerGlobalSourceEvent(form);
    }
  }

  ScmManagerSourceEvent(JSONObject form, Payload payload) {
    this(form.getString("server"), payload);
  }

  ScmManagerSourceEvent(String serverUrl, Payload payload) {
    super(Type.CREATED, payload, serverUrl);
    this.serverUrl = serverUrl;
  }

  @Override
  public boolean isMatch(@NonNull SCMNavigator navigator){
    return navigator instanceof ScmManagerNavigator && isMatch((ScmManagerNavigator) navigator);
  }

  private boolean isMatch(ScmManagerNavigator navigator) {
    return navigator.getServerUrl().startsWith(serverUrl) && isSpecificMatch(navigator);
  }

  abstract boolean isSpecificMatch(ScmManagerNavigator navigator);

  @Override
  public boolean isMatch(@NonNull SCMSource source){
    return source instanceof ScmManagerSource && isMatch((ScmManagerSource) source);
  }

  private boolean isMatch(@NonNull ScmManagerSource source){
    return
      // TODO SSH URLs?
      source.getServerUrl().startsWith(serverUrl) && isSpecificMatch(source);
  }

  abstract boolean isSpecificMatch(ScmManagerSource source);

  static class Payload {
    private final boolean global;

    Payload(boolean global) {
      this.global = global;
    }

    public boolean isGlobal() {
      return global;
    }
  }

  static class ScmManagerGlobalSourceEvent extends ScmManagerSourceEvent {
    public ScmManagerGlobalSourceEvent(JSONObject form) {
      super(form, new Payload(true));
    }

    @NonNull
    @Override
    public String getSourceName () {
      return "dummy";
    }

    @Override
    boolean isSpecificMatch(ScmManagerNavigator navigator) {
      return true;
    }

    @Override
    boolean isSpecificMatch(ScmManagerSource source) {
      return true;
    }
  }

  static class ScmManagerSingleSourceEvent extends ScmManagerSourceEvent {
    private final String namespace;
    private final String name;

    public ScmManagerSingleSourceEvent(JSONObject form) {
      super(form, new Payload(false));
      this.namespace = form.getString("namespace");
      this.name = form.getString("name");
    }

    @NonNull
    @Override
    public String getSourceName () {
      return name;
    }

    @Override
    boolean isSpecificMatch(ScmManagerNavigator navigator) {
      return navigator.getNamespace().equals(namespace);
    }

    @Override
    boolean isSpecificMatch(ScmManagerSource source) {
      return source.getName().equals(name);
    }
  }
}
