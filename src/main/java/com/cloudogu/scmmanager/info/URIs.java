package com.cloudogu.scmmanager.info;

import java.net.URI;

final class URIs {

  private URIs() {
  }

  static String normalize(String value) {
    URI uri = URI.create(value);
    String scheme = uri.getScheme();
    int port = uri.getPort();
    if (port < 0) {
      if ("http".equals(scheme)) {
        port = 80;
      } else if ("https".equals(scheme)) {
        port = 443;
      } else if ("ssh".equals(scheme)) {
        port = 22;
      }
    }
    return String.format(
      "%s://%s:%d%s", scheme, uri.getHost(), port, uri.getPath()
    );
  }
}
