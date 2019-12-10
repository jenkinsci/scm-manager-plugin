package com.cloudogu.scmmanager;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.jenkinsci.plugins.jsch.JSchConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.OutputStream;

public class ScmV2SshNotifier implements Notifier {

  private static final Logger LOG = LoggerFactory.getLogger(ScmV2SshNotifier.class);
  private static final String SSH_COMMAND = "scm ci-update --namespace %s --name %s --revision %s";

  private final NamespaceAndName namespaceAndName;
  private JSchConnector connector;

  ScmV2SshNotifier(NamespaceAndName namespaceAndName, JSchConnector connector) {
    this.namespaceAndName = namespaceAndName;
    this.connector = connector;
  }

  @Override
  public void notify(String revision, BuildStatus buildStatus) throws IOException, JSchException, JAXBException {
    LOG.info("set rev {} of {} to {}", revision, namespaceAndName, buildStatus.getStatus());
    Session session = connector.getSession();
    session.connect(5000);
    executeNotifyViaChannel(session, revision, buildStatus);
    session.disconnect();
  }

  private void executeNotifyViaChannel(Session session, String revision, BuildStatus buildStatus) throws JSchException, IOException, JAXBException {
    Channel channel = session.openChannel("exec");
    ((ChannelExec) channel).setCommand(String.format(SSH_COMMAND, namespaceAndName.getNamespace(), namespaceAndName.getName(), revision));
    setBuildStatusTypeIfNull(buildStatus);
    channel.connect();
    marshalBuildStatusIntoOutputstream(channel, buildStatus);
    channel.disconnect();
  }

  private void setBuildStatusTypeIfNull(BuildStatus buildStatus) {
    if (buildStatus.getType() == null) {
      buildStatus.setType("jenkins");
    }
  }

  private void marshalBuildStatusIntoOutputstream(Channel channel, BuildStatus buildStatus) throws IOException, JAXBException {
    OutputStream out = channel.getOutputStream();
    JAXBContext jaxbContext = JAXBContext.newInstance(BuildStatus.class);
    Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.marshal(buildStatus, out);
    out.flush();
  }
}
