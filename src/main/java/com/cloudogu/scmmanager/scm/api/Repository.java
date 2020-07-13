package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;

public class Repository extends HalRepresentation {

  public ApiClient client;

  private String namespace;
  private String name;
  private String type;

  private CloneInformation cloneInformation;

  public Repository() {
  }

  public Repository(String namespace, String name, String type) {
    this.namespace = namespace;
    this.name = name;
    this.type = type;
  }

  void setClient(ApiClient client) {
    this.client = client;
  }

  public String getType() {
    return type;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return getLinks()
      .getLinkBy("protocol", l -> "http".equals(l.getName()))
      .orElseThrow(() -> new IllegalStateException("no http url"))
      .getHref();
  }

  public CloneInformation getCloneInformation() {
    if (cloneInformation == null) {
      cloneInformation = new CloneInformation(type, getUrl());
    }
    return cloneInformation;
  }
}
