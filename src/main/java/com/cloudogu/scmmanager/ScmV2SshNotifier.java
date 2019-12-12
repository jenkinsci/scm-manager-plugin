package com.cloudogu.scmmanager;

import com.google.common.annotations.VisibleForTesting;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.OutputStream;

public class ScmV2SshNotifier implements Notifier {

  private static final Logger LOG = LoggerFactory.getLogger(ScmV2SshNotifier.class);
  private static final String SSH_COMMAND = "scm ci-update --namespace %s --name %s --revision %s";

  private final NamespaceAndName namespaceAndName;
  private final Connection connection;
  private final SSHAuthentication authentication;

  ScmV2SshNotifier(NamespaceAndName namespaceAndName, Connection connection, SSHAuthentication authentication) {
    this.namespaceAndName = namespaceAndName;
    this.connection = connection;
    this.authentication = authentication;
  }

  @VisibleForTesting
  public NamespaceAndName getNamespaceAndName() {
    return namespaceAndName;
  }

  @Override
  public void notify(String revision, BuildStatus buildStatus) throws IOException {
    LOG.info("set rev {} of {} to {}", revision, namespaceAndName, buildStatus.getStatus());
    try {
      connect();
      executeStatusUpdateCommand(connection, revision, buildStatus);
    } finally {
      connection.close();
    }
  }

  private void connect() throws SshConnectionFailedException {
    try {
      // accept any host
      connection.connect((s, i, s1, bytes) -> true);
      authentication.authenticate(connection);
    } catch (IOException ex) {
      throw new SshConnectionFailedException("ssh connection failed", ex);
    }
  }

  private void executeStatusUpdateCommand(Connection connection, String revision, BuildStatus buildStatus) throws IOException {
    String cmd = String.format(SSH_COMMAND, namespaceAndName.getNamespace(), namespaceAndName.getName(), revision);

    Session session = null;
    try {
      session = connection.openSession();
      session.execCommand(cmd);
      setBuildStatusTypeIfNull(buildStatus);
      marshalBuildStatusIntoOutputStream(session, buildStatus);
    } finally {
      if (session != null) {
        session.close();
      }
    }
  }

  private void setBuildStatusTypeIfNull(BuildStatus buildStatus) {
    if (buildStatus.getType() == null) {
      buildStatus.setType("jenkins");
    }
  }

  private void marshalBuildStatusIntoOutputStream(Session session, BuildStatus buildStatus) throws IOException {
    try (OutputStream out = session.getStdin()) {
      JAXB.marshal(buildStatus, out);
      out.flush();
    }
  }
}
