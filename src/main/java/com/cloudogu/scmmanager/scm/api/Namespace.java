package com.cloudogu.scmmanager.scm.api;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;

import java.io.Serializable;

public class Namespace extends HalRepresentation implements Serializable {
  private String namespace;

  public Namespace() {
  }

  public Namespace(String namespace) {
    this.namespace = namespace;
  }

  @VisibleForTesting
  public Namespace(Links links, String namespace) {
    super(links);
    this.namespace = namespace;
  }

  public String getNamespace() {
    return namespace;
  }
}
