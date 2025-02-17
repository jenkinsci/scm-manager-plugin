package com.cloudogu.scmmanager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.Test;

public class SshConnectionFactoryTest {

    private final SshConnectionFactory factory = new SshConnectionFactory();

    @Test
    public void shouldCreateSshConnectionWithoutRepository() {
        shouldCreateSshConnectionWithoutRepository("ssh://scm.hitchhiker.com:2222");
        shouldCreateSshConnectionWithoutRepository("ssh://scm.hitchhiker.com:2222/");
    }

    public void shouldCreateSshConnectionWithoutRepository(String url) {
        Optional<SshConnection> optionalSshConnection = factory.create(url);
        assertThat(optionalSshConnection).isPresent();
        SshConnection sshConnection = optionalSshConnection.get();

        assertThat(sshConnection.getRepository()).isEmpty();
        assertThat(sshConnection.getConnection().getHostname()).isEqualTo("scm.hitchhiker.com");
        assertThat(sshConnection.getConnection().getPort()).isEqualTo(2222);

        IllegalStateException exception = null;
        try {
            sshConnection.mustGetRepository();
        } catch (IllegalStateException ex) {
            exception = ex;
        }
        assertThat(exception).isNotNull();
    }

    @Test
    public void shouldCreateSshConnectionWithRepository() {
        Optional<SshConnection> optionalSshConnection =
                factory.create("ssh://scm.hitchhiker.com:2222/repo/spaceships/heart-of-gold");
        assertThat(optionalSshConnection).isPresent();
        SshConnection sshConnection = optionalSshConnection.get();

        NamespaceAndName repository = sshConnection.mustGetRepository();
        assertThat(repository.getNamespace()).isEqualTo("spaceships");
        assertThat(repository.getName()).isEqualTo("heart-of-gold");
        assertThat(sshConnection.getConnection().getHostname()).isEqualTo("scm.hitchhiker.com");
        assertThat(sshConnection.getConnection().getPort()).isEqualTo(2222);
    }

    @Test
    public void shouldCreateSshConnectionWithDefaultPort() {
        Optional<SshConnection> optionalSshConnection =
                factory.create("ssh://scm.hitchhiker.com/repo/spaceships/heart-of-gold");
        assertThat(optionalSshConnection).isPresent();
        assertThat(optionalSshConnection.get().getConnection().getPort()).isEqualTo(22);
    }

    @Test
    public void shouldReturnEmptyForNonSshConnections() {
        Optional<SshConnection> optionalSshConnection =
                factory.create("https://scm.hitchhiker.com/repo/spaceships/heart-of-gold");
        assertThat(optionalSshConnection).isEmpty();
    }
}
