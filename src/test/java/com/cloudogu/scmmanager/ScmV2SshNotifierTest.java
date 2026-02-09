package com.cloudogu.scmmanager;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScmV2SshNotifierTest {

    @Mock
    private Connection connection;

    @Mock
    private SSHAuthentication authentication;

    @Test
    void testNotify() throws IOException {
        Session sessionMock = Mockito.mock(Session.class);
        when(connection.openSession()).thenReturn(sessionMock);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(sessionMock.getStdin()).thenReturn(out);

        ScmV2SshNotifier scmV2SshNotifier = createNotifier();
        scmV2SshNotifier.notify("1a2b3c4d5e6f", createBuildStatus(true));
        verify(sessionMock).execCommand(any());
    }

    private ScmV2SshNotifier createNotifier() {
        SshConnection sshConnection = new SshConnection(connection, new NamespaceAndName("space", "name"));
        return new ScmV2SshNotifier(sshConnection, authentication);
    }

    @Test
    void shouldSetTypeToJenkinsIfNoTypeAvailable() throws IOException {
        Session sessionMock = Mockito.mock(Session.class);
        when(connection.openSession()).thenReturn(sessionMock);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(sessionMock.getStdin()).thenReturn(out);

        ScmV2SshNotifier scmV2SshNotifier = createNotifier();
        scmV2SshNotifier.notify("1a2b3c4d5e6f", createBuildStatus(false));

        assertTrue(out.toString().contains("jenkins"));
        assertTrue(out.toString().contains("Jenkins SCM Plugin"));
        assertTrue(out.toString().contains("jenkins-scm-plugin"));
        assertTrue(out.toString().contains("jenkins.io"));
        assertTrue(out.toString().contains("SUCCESS"));
    }

    private BuildStatus createBuildStatus(boolean withType) {
        BuildStatus buildStatus = new BuildStatus();
        buildStatus.setDisplayName("Jenkins SCM Plugin");
        buildStatus.setName("jenkins-scm-plugin");
        buildStatus.setUrl("jenkins.io/scm-plugin");
        if (withType) {
            buildStatus.setType("jenkins");
        }
        buildStatus.setStatus(BuildStatus.StatusType.SUCCESS);
        return buildStatus;
    }
}
