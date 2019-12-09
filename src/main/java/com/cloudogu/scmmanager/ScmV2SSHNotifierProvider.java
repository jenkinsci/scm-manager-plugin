package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.ScmInformation;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import hudson.Extension;
import hudson.model.Run;
import org.jenkinsci.plugins.jsch.JSchConnector;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Extension
public class ScmV2SSHNotifierProvider implements NotifierProvider {

  private static final Pattern PATTERN = Pattern.compile("^ssh://(.*@)?([^:]+)(:[0-9]*)?/repo/(.+)/(.+)$");

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

    JSchConnector connector = createSSHConnector(matcher, information, run);
    return new ScmV2SshNotifier(namespaceAndName, connector);
  }

  private JSchConnector createSSHConnector(Matcher matcher, ScmInformation information, Run<?, ?> run) throws JSchException {
    JSchConnector connector = new JSchConnector("scmadmin", matcher.group(2), getPort(matcher));

//    SSHUserPrivateKey credentialById = CredentialsProvider.findCredentialById(information.getCredentialsId(), SSHUserPrivateKey.class, run, Collections.emptyList());
//    connector.getJSch().addIdentity("scmadmin",credentialById.getPrivateKeys().get(0).getBytes(),"".getBytes(),"".getBytes());
    connector.getJSch().addIdentity("~/scmadmin");
    Session session = connector.getSession();
    java.util.Properties config = new java.util.Properties();
    config.put("StrictHostKeyChecking", "no");
    config.put("PreferredAuthentications","publickey,password");
    session.setConfig(config);
    return connector;
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
