package com.cloudogu.scmmanager.scm.api;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Data;

@Data
public class Index extends HalRepresentation {
  private String version;

  public Index() { }

  public Index(Links links) {
    super(links);
  }
}
