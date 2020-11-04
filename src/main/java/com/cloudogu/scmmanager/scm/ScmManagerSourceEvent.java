package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceEvent;
import net.sf.json.JSONObject;

public abstract class ScmManagerSourceEvent extends SCMSourceEvent<ScmManagerSourceEvent.Payload> {

  private final ServerIdentification identification;

  public static ScmManagerSourceEvent from(JSONObject form) {
    if (form.containsKey("namespace") && form.containsKey("name")) {
      return new ScmManagerSingleSourceEvent(form);
    } else {
      return new ScmManagerGlobalSourceEvent(form);
    }
  }

  ScmManagerSourceEvent(JSONObject form, Payload payload) {
    this(new ServerIdentification(form), payload);
  }

  ScmManagerSourceEvent(ServerIdentification identification, Payload payload) {
    super(Type.CREATED, payload, identification.getServerUrl());
    this.identification = identification;
  }

  @Override
  public boolean isMatch(@NonNull SCMNavigator navigator){
    return navigator instanceof ScmManagerNavigator && isMatch((ScmManagerNavigator) navigator);
  }

  private boolean isMatch(ScmManagerNavigator navigator) {
    return identification.matches(navigator.getServerUrl()) && isSpecificMatch(navigator);
  }

  abstract boolean isSpecificMatch(ScmManagerNavigator navigator);

  @Override
  public boolean isMatch(@NonNull SCMSource source){
    return source instanceof ScmManagerSource && isMatch((ScmManagerSource) source);
  }

  private boolean isMatch(@NonNull ScmManagerSource source){
    return identification.matches(source.getServerUrl()) && isSpecificMatch(source);
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
