package com.cloudogu.scmmanager;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.tidy.Out;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmV2SshNotifierTest {

  @Mock
  Connection connection;

  @Mock
  SSHAuthentication authentication;

  @Test
  public void testNotify() throws IOException {
    Session sessionMock = Mockito.mock(Session.class);
    when(connection.openSession()).thenReturn(sessionMock);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    when(sessionMock.getStdin()).thenReturn(out);
    ScmV2SshNotifier scmV2SshNotifier = new ScmV2SshNotifier(new NamespaceAndName("space", "name"), connection, authentication);
    scmV2SshNotifier.notify("1a2b3c4d5e6f", createBuildStatus(true));
    verify(sessionMock).execCommand(any());
  }

  @Test
  public void shouldSetTypeToJenkinsIfNoTypeAvailable() throws IOException {
    Session sessionMock = Mockito.mock(Session.class);
    when(connection.openSession()).thenReturn(sessionMock);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    when(sessionMock.getStdin()).thenReturn(out);

    ScmV2SshNotifier scmV2SshNotifier = new ScmV2SshNotifier(new NamespaceAndName("space", "name"), connection, authentication);
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
