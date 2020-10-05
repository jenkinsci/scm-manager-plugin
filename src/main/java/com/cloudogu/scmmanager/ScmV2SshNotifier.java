package com.cloudogu.scmmanager;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ScmV2SshNotifier implements Notifier {

  private static final Logger LOG = LoggerFactory.getLogger(ScmV2SshNotifier.class);
  private static final String SSH_COMMAND = "scm-ci-update --namespace %s --name %s --revision %s";

  private final SshConnection connection;
  private final SSHAuthentication authentication;

  ScmV2SshNotifier(SshConnection connection, SSHAuthentication authentication) {
    this.connection = connection;
    this.authentication = authentication;
  }

  @VisibleForTesting
  public SshConnection getConnection() {
    return connection;
  }

  @Override
  public void notify(String revision, BuildStatus buildStatus) throws IOException {
    LOG.info("set rev {} of {} to {}", revision, connection.getRepository(), buildStatus.getStatus());
    try {
      connection.connect(authentication);
      executeStatusUpdateCommand(revision, buildStatus);
    } finally {
      connection.close();
    }
  }

  private void executeStatusUpdateCommand(String revision, BuildStatus buildStatus) throws IOException {
    String cmd = createCommand(revision);
    setBuildStatusTypeIfNull(buildStatus);
    connection.command(cmd)
      .withInput(buildStatus).xml()
      .exec();
  }

  private String createCommand(String revision) {
    return String.format(SSH_COMMAND, connection.getRepository().getNamespace(), connection.getRepository().getName(), revision);
  }

  private void setBuildStatusTypeIfNull(BuildStatus buildStatus) {
    if (buildStatus.getType() == null) {
      buildStatus.setType("jenkins");
    }
  }
}
