package com.cloudogu.scmmanager.scm.api;

import de.otto.edison.hal.HalRepresentation;

import java.util.Objects;

public class Repository extends HalRepresentation {

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Repository that = (Repository) o;
    return Objects.equals(namespace, that.namespace) &&
      Objects.equals(name, that.name) &&
      Objects.equals(type, that.type) &&
      Objects.equals(cloneInformation, that.cloneInformation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), namespace, name, type, cloneInformation);
  }
}
