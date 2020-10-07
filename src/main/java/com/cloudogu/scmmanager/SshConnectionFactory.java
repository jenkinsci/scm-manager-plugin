package com.cloudogu.scmmanager;

import com.google.common.base.Strings;
import com.trilead.ssh2.Connection;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class SshConnectionFactory {

  private static final Pattern PATTERN = Pattern.compile("^ssh://(.*@)?([^/:]+)(:[0-9]*)?(:?/repo/([^/]+)/([^/]+))?/?$");

  public SshConnectionFactory() {
  }

  public Optional<SshConnection> create(String url) {
    Matcher matcher = PATTERN.matcher(url);
    if (matcher.matches()) {
      return of(createSshConnection(matcher));
    }
    return empty();
  }

  private SshConnection createSshConnection(Matcher matcher) {
    return new SshConnection(
      createConnection(matcher),
      createRepository(matcher)
    );
  }

  private Connection createConnection(Matcher matcher) {
    return new Connection(matcher.group(2), getPort(matcher));
  }

  private Integer getPort(Matcher matcher) {
    String group = matcher.group(3);
    if (Strings.isNullOrEmpty(group)) {
      return 22;
    }
    return Integer.valueOf(group.substring(1));
  }

  private NamespaceAndName createRepository(Matcher matcher) {
    String namespace = matcher.group(5);
    String name = matcher.group(6);

    // ssh connection url without repository suffix
    if (namespace == null && name == null) {
      return null;
    } else if (name.endsWith(".git")) {
      return new NamespaceAndName(namespace, name.substring(0, name.length() - ".git".length()));
    } else {
      return new NamespaceAndName(namespace, name);
    }
  }

}
