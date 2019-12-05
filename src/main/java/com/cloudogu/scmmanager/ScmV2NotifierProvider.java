package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.ScmInformation;
import com.google.common.base.Strings;
import com.jcraft.jsch.JSchException;
import hudson.Extension;
import hudson.model.Run;
import org.jenkinsci.plugins.jsch.JSchConnector;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Extension
public class ScmV2NotifierProvider implements NotifierProvider {

  private static final Pattern PATTERN = Pattern.compile("^http(?:s)?://[^/]+(/[A-Za-z0-9.\\-_]+)?/repo/([A-Za-z0-9.\\-_]+)/([A-Za-z0-9.\\-_]+)(?:/.*)?$");

  private AuthenticationFactory authenticationFactory;

  @Inject
  public void setAuthenticationFactory(AuthenticationFactory authenticationFactory) {
    this.authenticationFactory = authenticationFactory;
  }

  @Override
  public Optional<ScmV2Notifier> get(Run<?, ?> run, ScmInformation information) throws MalformedURLException, JSchException {
    String url = information.getUrl();
    Matcher matcher = PATTERN.matcher(url);
    if (matcher.matches()) {
      return of(createNotifier(run, information, url, matcher));
    }
    return empty();
  }

  private ScmV2Notifier createNotifier(Run<?, ?> run, ScmInformation information, String url, Matcher matcher) throws MalformedURLException, JSchException {
    URL instance = createInstanceURL(url, matcher);
    NamespaceAndName namespaceAndName = createNamespaceAndName(matcher);

    JSchConnector connector = createSSHConnector(instance, information);
    if (connector.hasSession()) {
      return new ScmV2Notifier(instance, namespaceAndName, connector);
    } else {
      Authentication authentication = authenticationFactory.create(run, information.getCredentialsId());
      return new ScmV2Notifier(instance, namespaceAndName, authentication);
    }
  }

  private JSchConnector createSSHConnector(URL instance, ScmInformation information) {
    JSchConnector connector = new JSchConnector(information.getCredentialsId(), instance.getHost(), instance.getPort());
    connector.getSession().setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
    java.util.Properties config = new java.util.Properties();
    config.put("StrictHostKeyChecking", "no");
    connector.getSession().setConfig(config);
    return connector;
  }

  private NamespaceAndName createNamespaceAndName(Matcher matcher) {
    String namespace = matcher.group(2);
    String name = matcher.group(3);
    if (name.endsWith(".git")) {
      return new NamespaceAndName(namespace, name.substring(0, name.length() - ".git".length()));
    } else {
      return new NamespaceAndName(namespace, name);
    }
  }

  private URL createInstanceURL(String stringUrl, Matcher matcher) throws MalformedURLException {
    URL url = new URL(stringUrl);
    return new URL(url.getProtocol(), url.getHost(), url.getPort(), Strings.nullToEmpty(matcher.group(1)));
  }
}
