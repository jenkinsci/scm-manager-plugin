package com.cloudogu.scmmanager;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.jenkinsci.plugins.jsch.JSchConnector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmV2SshNotifierTest {

  @Mock
  JSchConnector connector;

  @Test
  public void testNotify() throws JSchException, JAXBException, IOException {
    Session sessionMock = Mockito.mock(Session.class);
    when(connector.getSession()).thenReturn(sessionMock);
    ChannelExec channelExecMock = Mockito.mock(ChannelExec.class);
    when(sessionMock.openChannel("exec")).thenReturn(channelExecMock);
    OutputStream outMock = Mockito.mock(OutputStream.class);
    when(channelExecMock.getOutputStream()).thenReturn(outMock);

    ScmV2SshNotifier scmV2SshNotifier = new ScmV2SshNotifier(new NamespaceAndName("space", "name"), connector, authentication);
    scmV2SshNotifier.notify("1a2b3c4d5e6f", createBuildStatus(true));

    verify(outMock).flush();
  }

  @Test
  public void shouldSetTypeToJenkinsIfNoTypeAvailable() throws JSchException, JAXBException, IOException {
    Session sessionMock = Mockito.mock(Session.class);
    when(connector.getSession()).thenReturn(sessionMock);
    ChannelExec channelExecMock = Mockito.mock(ChannelExec.class);
    when(sessionMock.openChannel("exec")).thenReturn(channelExecMock);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    when(channelExecMock.getOutputStream()).thenReturn(out);

    ScmV2SshNotifier scmV2SshNotifier = new ScmV2SshNotifier(new NamespaceAndName("space", "name"), connector, authentication);
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
