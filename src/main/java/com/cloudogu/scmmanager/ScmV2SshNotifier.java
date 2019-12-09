package com.cloudogu.scmmanager;

import com.google.common.annotations.VisibleForTesting;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ning.http.client.Response;
import org.jenkinsci.plugins.jsch.JSchConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

public class ScmV2SshNotifier implements Notifier {

  private static final Logger LOG = LoggerFactory.getLogger(ScmV2SshNotifier.class);

  private static final String URL = "%s/api/v2/ci/%s/%s/changesets/%s/%s/%s";
  private static final String SSH_COMMAND = "scm ci-update --namespace %s --name %s --revision %s";

  private final NamespaceAndName namespaceAndName;
  private JSchConnector connector;

  private Consumer<Response> completionListener = response -> {
  };

  ScmV2SshNotifier(NamespaceAndName namespaceAndName, JSchConnector connector) {
    this.namespaceAndName = namespaceAndName;
    this.connector = connector;
  }

  @VisibleForTesting
  NamespaceAndName getNamespaceAndName() {
    return namespaceAndName;
  }

  @VisibleForTesting
  void setCompletionListener(Consumer<Response> completionListener) {
    this.completionListener = completionListener;
  }


  @Override
  public void notify(String revision, BuildStatus buildStatus) throws IOException, JSchException, JAXBException {
    LOG.info("set rev {} of {} to {}", revision, namespaceAndName, buildStatus.getStatus());
    Session session = connector.getSession();
    session.connect(5000);
    Channel channel = session.openChannel("exec");
    ((ChannelExec) channel).setCommand(String.format(SSH_COMMAND, namespaceAndName.getNamespace(), namespaceAndName.getName(), revision));
    if (buildStatus.getType() == null) {
      buildStatus.setType("jenkins");
    }
    channel.connect();
    OutputStream out = channel.getOutputStream();
    JAXBContext jaxbContext = JAXBContext.newInstance(BuildStatus.class);
    Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.marshal(buildStatus, out);
    out.flush();
    channel.disconnect();
    session.disconnect();
  }
}
