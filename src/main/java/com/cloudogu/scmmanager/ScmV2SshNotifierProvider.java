package com.cloudogu.scmmanager;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudogu.scmmanager.info.ScmInformation;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import hudson.Extension;
import hudson.model.Run;
import org.jenkinsci.plugins.jsch.JSchConnector;

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
  public Optional<ScmV2SshNotifier> get(Run<?, ?> run, ScmInformation information) throws JSchException {
    String url = information.getUrl();
    Matcher matcher = PATTERN.matcher(url);
    if (matcher.matches()) {
      return of(createNotifier(run, information, matcher));
    }
    return empty();
  }

  private ScmV2SshNotifier createNotifier(Run<?, ?> run, ScmInformation information, Matcher matcher) throws JSchException {
    NamespaceAndName namespaceAndName = createNamespaceAndName(matcher);
    SSHAuthentication authentication = authenticationFactory.createSSH(run, information.getCredentialsId());
    JSchConnector connector;
    if (authentication instanceof SshPrivateKeyAuthentication) {
      connector = createPrivateKeyConnector(matcher, (SshPrivateKeyAuthentication) authentication);
    } else if (authentication instanceof SshUsernamePasswordAuthentication) {
      connector = createUsernamePasswordConnector(matcher, (SshUsernamePasswordAuthentication) authentication);
    } else {
      throw new CredentialsUnavailableException("invalid credentials for ssh connection");
    }
    setupSessionConfig(connector.getSession());

    return new ScmV2SshNotifier(namespaceAndName, connector);
  }

  private JSchConnector createUsernamePasswordConnector(Matcher matcher, SshUsernamePasswordAuthentication authentication) {
    JSchConnector connector;
    connector = createConnector(authentication.getUsername(), matcher);
    connector.getSession().setPassword(authentication.getPassword());
    return connector;
  }

  private JSchConnector createPrivateKeyConnector(Matcher matcher, SshPrivateKeyAuthentication authentication) throws JSchException {
    JSchConnector connector;
    connector = createConnector(authentication.getUsername(), matcher);
    connector.getJSch().addIdentity(authentication.getUsername(), authentication.getPrivateKey().getBytes(), "".getBytes(), "".getBytes());
    return connector;
  }

  private JSchConnector createConnector(String username, Matcher matcher) {
    return new JSchConnector(username, matcher.group(2), getPort(matcher));
  }

  private void setupSessionConfig(Session session) {
    java.util.Properties config = new java.util.Properties();
    config.put("StrictHostKeyChecking", "no");
    config.put("PreferredAuthentications", "publickey,password");
    session.setConfig(config);
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
