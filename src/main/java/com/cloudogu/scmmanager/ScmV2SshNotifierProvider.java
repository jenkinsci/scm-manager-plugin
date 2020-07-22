package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.JobInformation;
import com.trilead.ssh2.Connection;
import hudson.Extension;
import hudson.model.Run;

import javax.inject.Inject;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Extension
public class ScmV2SshNotifierProvider implements NotifierProvider {

  private static final Pattern PATTERN = Pattern.compile("^ssh://(.*@)?([^:]+)(:[0-9]*)?/repo/(.+)/(.+)$");

  private AuthenticationFactory authenticationFactory;

  @Inject
  public void setAuthenticationFactory(AuthenticationFactory authenticationFactory) {
    this.authenticationFactory = authenticationFactory;
  }

  @Override
  public Optional<ScmV2SshNotifier> get(Run<?, ?> run, JobInformation information) {
    String url = information.getUrl();
    Matcher matcher = PATTERN.matcher(url);
    if (matcher.matches()) {
      return of(createNotifier(run, information, matcher));
    }
    return empty();
  }

  private ScmV2SshNotifier createNotifier(Run<?, ?> run, JobInformation information, Matcher matcher)  {
    NamespaceAndName namespaceAndName = createNamespaceAndName(matcher);
    SSHAuthentication authentication = authenticationFactory.createSSH(run, information.getCredentialsId());
    Connection connection = new Connection(matcher.group(2), getPort(matcher));
    return new ScmV2SshNotifier(namespaceAndName, connection, authentication);
  }

  private Integer getPort(Matcher matcher) {
    return Integer.valueOf(matcher.group(3).substring(1));
  }

  private NamespaceAndName createNamespaceAndName(Matcher matcher) {
    String namespace = matcher.group(4);
    String name = matcher.group(5);
    if (name.endsWith(".git")) {
      return new NamespaceAndName(namespace, name.substring(0, name.length() - ".git".length()));
    } else {
      return new NamespaceAndName(namespace, name);
    }
  }
}
