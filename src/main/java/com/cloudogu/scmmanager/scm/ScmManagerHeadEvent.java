package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.scm.SCM;
import jenkins.scm.api.SCMEvent;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.Stapler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

abstract class ScmManagerHeadEvent extends SCMHeadEvent<ScmManagerHeadEvent.TriggerPayload> {

  private final String namespace;
  private final String name;
  private final String type;
  private final String serverUrl;

  ScmManagerHeadEvent(Type changeType, JSONObject form) {
    this(changeType, form.getString("namespace"),
      form.getString("name"),
      form.getString("type"),
      form.getString("server"));
  }

  ScmManagerHeadEvent(Type changeType, String namespace, String name, String type, String serverUrl) {
    super(changeType, new TriggerPayload(namespace, name), SCMEvent.originOf(Stapler.getCurrentRequest()));
    this.namespace = namespace;
    this.name = name;
    this.type = type;
    this.serverUrl = serverUrl;
  }

  @Override
  public boolean isMatch(@NonNull SCMNavigator navigator) {
    return false;
  }

  @NonNull
  @Override
  public String getSourceName() {
    return "dummy";
  }

  @NonNull
  @Override
  public Map<SCMHead, SCMRevision> heads(@NonNull SCMSource source) {
    ScmManagerSource scmManagerSource = (ScmManagerSource) source;
    CloneInformation cloneInformation = new CloneInformation(scmManagerSource.getType(), scmManagerSource.getServerUrl());
    Collection<SCMHead> heads = heads(cloneInformation);
    HashMap<SCMHead, SCMRevision> map = new HashMap<>();
    heads.forEach(head -> map.put(head, null));
    return map;
  }

  abstract Collection<SCMHead> heads(CloneInformation cloneInformation);

  @Override
  public boolean isMatch(@NonNull SCMSource source) {
    // TODO SVN?
    return source instanceof ScmManagerSource && isMatch((ScmManagerSource) source);
  }

  private boolean isMatch(@NonNull ScmManagerSource source) {
    return source.getRepository().equals(String.format("%s/%s/%s", namespace, name, type))
    // TODO SSH URLs?
      && source.getServerUrl().startsWith(serverUrl);
  }

  @Override
  public boolean isMatch(@NonNull SCM scm) {
    return false;
  }

  public static class TriggerPayload {
    private final String namespace;
    private final String name;

    public TriggerPayload(String namespace, String name) {
      this.namespace = namespace;
      this.name = name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TriggerPayload that = (TriggerPayload) o;
      return Objects.equals(namespace, that.namespace) &&
        Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(namespace, name);
    }
  }
}
