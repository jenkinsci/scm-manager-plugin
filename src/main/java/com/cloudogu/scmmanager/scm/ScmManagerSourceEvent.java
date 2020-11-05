package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceEvent;
import net.sf.json.JSONObject;

public abstract class ScmManagerSourceEvent extends SCMSourceEvent<ScmManagerSourceEvent.TriggerPayload> {

  private final ServerIdentification identification;

  public static ScmManagerSourceEvent from(JSONObject form) {
    if (form.containsKey("namespace") && form.containsKey("name")) {
      return new ScmManagerSingleSourceEvent(form);
    } else {
      return new ScmManagerGlobalSourceEvent(form);
    }
  }

  ScmManagerSourceEvent(JSONObject form, TriggerPayload payload) {
    this(new ServerIdentification(form), payload);
  }

  ScmManagerSourceEvent(ServerIdentification identification, TriggerPayload payload) {
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

  static class TriggerPayload {
    private final boolean global;
    private final String namespace;
    private final String name;

    TriggerPayload() {
      this(true, null, null);
    }

    public TriggerPayload(String namespace, String name) {
      this(false, namespace, name);
    }

    public TriggerPayload(boolean global, String namespace, String name){
        this.global = global;
        this.namespace = namespace;
        this.name = name;
      }

      public boolean isGlobal () {
        return global;
      }

    public String getNamespace() {
      return namespace;
    }

    public String getName() {
      return name;
    }
  }

  static class ScmManagerGlobalSourceEvent extends ScmManagerSourceEvent {
    public ScmManagerGlobalSourceEvent(JSONObject form) {
      super(form, new TriggerPayload());
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

    public ScmManagerSingleSourceEvent(JSONObject form) {
      super(form, new TriggerPayload(form.getString("namespace"), form.getString("name")));
    }

    @NonNull
    @Override
    public String getSourceName () {
      return getPayload().getName();
    }

    @Override
    boolean isSpecificMatch(ScmManagerNavigator navigator) {
      return navigator.getNamespace().equals(getPayload().getNamespace());
    }

    @Override
    boolean isSpecificMatch(ScmManagerSource source) {
      return source.getName().equals(getPayload().getName());
    }
  }
}
