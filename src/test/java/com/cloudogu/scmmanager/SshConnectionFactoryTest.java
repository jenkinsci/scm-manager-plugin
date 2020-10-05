package com.cloudogu.scmmanager;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class SshConnectionFactoryTest {

  @Test
  public void shouldCreateSshConnection() {
    Optional<SshConnection> optionalSshConnection = SshConnectionFactory.create("ssh://scm.hitchhiker.com:2222/repo/spaceships/heart-of-gold");
    assertThat(optionalSshConnection).isPresent();
    SshConnection sshConnection = optionalSshConnection.get();
    assertThat(sshConnection.getRepository().getNamespace()).isEqualTo("spaceships");
    assertThat(sshConnection.getRepository().getName()).isEqualTo("heart-of-gold");
    assertThat(sshConnection.getConnection().getHostname()).isEqualTo("scm.hitchhiker.com");
    assertThat(sshConnection.getConnection().getPort()).isEqualTo(2222);
  }

  @Test
  public void shouldCreateSshConnectionWithDefaultPort() {
    Optional<SshConnection> optionalSshConnection = SshConnectionFactory.create("ssh://scm.hitchhiker.com/repo/spaceships/heart-of-gold");
    assertThat(optionalSshConnection).isPresent();
    assertThat(optionalSshConnection.get().getConnection().getPort()).isEqualTo(22);
  }

  @Test
  public void shouldReturnEmptyForNonSshConnections() {
    Optional<SshConnection> optionalSshConnection = SshConnectionFactory.create("https://scm.hitchhiker.com/repo/spaceships/heart-of-gold");
    assertThat(optionalSshConnection).isEmpty();
  }

}
