package com.cloudogu.scmmanager;

public class NamespaceAndName {

  private final String namespace;
  private final String name;

  public NamespaceAndName(String namespace, String name) {
    this.namespace = namespace;
    this.name = name;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return namespace + "/" + name;
  }
}
