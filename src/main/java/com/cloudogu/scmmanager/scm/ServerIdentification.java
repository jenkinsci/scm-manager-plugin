package com.cloudogu.scmmanager.scm;

import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

class ServerIdentification {

  private static final Logger LOG = LoggerFactory.getLogger(ServerIdentification.class);

  private final String serverUrl;
  private final Collection<Identification> identifications;

  public ServerIdentification(JSONObject form) {
    this(form.getString("server"), readIdentifications(form));
  }

  private static Collection<Identification> readIdentifications(JSONObject form) {
    if (form.containsKey("identifications")) {
      return form.getJSONArray("identifications").stream()
        .filter(JSONObject.class::isInstance)
        .map(JSONObject.class::cast)
        .filter(o -> o.containsKey("value") && o.containsKey("name"))
        .map(Identification::new)
        .collect(Collectors.toList());
    } else {
      return emptySet();
    }
  }

  ServerIdentification(String serverUrl, Collection<Identification> identifications) {
    this.serverUrl = serverUrl;
    this.identifications = identifications;
  }

  boolean matches(String serverUrl) {
    if (serverUrl != null) {
      return serverUrl.startsWith(this.serverUrl) || identifications.stream().anyMatch(i -> i.matches(serverUrl));
    }
    return false;
  }

  String getServerUrl() {
    return serverUrl;
  }

  private static class Identification {
    private final String name;
    private final String value;

    private Identification(JSONObject object) {
      this(object.getString("name"), object.getString("value"));
    }

    private Identification(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public boolean matches(String serverUrl) {
      boolean contains = serverUrl.contains(value);
      if (contains) {
        LOG.debug("found matching server url {} in hook by identification {} with value {}", serverUrl, name, value);
      }
      return contains;
    }
  }
}
