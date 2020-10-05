package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.JobInformation;
import hudson.Extension;
import hudson.model.Run;

import javax.inject.Inject;
import java.util.Optional;

@Extension
public class ScmV2SshNotifierProvider implements NotifierProvider {

  private AuthenticationFactory authenticationFactory;

  @Inject
  public void setAuthenticationFactory(AuthenticationFactory authenticationFactory) {
    this.authenticationFactory = authenticationFactory;
  }

  @Override
  public Optional<ScmV2SshNotifier> get(Run<?, ?> run, JobInformation information) {
    return SshConnectionFactory.create(information.getUrl())
      .map(sshConnection -> createNotifier(run, information, sshConnection));
  }

  private ScmV2SshNotifier createNotifier(Run<?, ?> run, JobInformation information, SshConnection connection)  {
    SSHAuthentication authentication = authenticationFactory.createSSH(run, information.getCredentialsId());
    return new ScmV2SshNotifier(connection, authentication);
  }

}
