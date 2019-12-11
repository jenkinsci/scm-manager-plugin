package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.ScmInformation;
import com.google.common.io.Resources;
import com.jcraft.jsch.JSchException;
import hudson.model.Run;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmV2SshNotifierProviderTest {

  @InjectMocks
  private ScmV2SshNotifierProvider provider;

  @Mock
  private AuthenticationFactory authenticationFactory;

  @Mock
  private Run<?, ?> run;

  @Test
  public void testGetWithoutMatchingNotifier() throws JSchException {
    ScmInformation information = createInformation("sample://one");
    Optional<ScmV2SshNotifier> notifier = provider.get(run, information);
    assertFalse(notifier.isPresent());
  }

  @Test
  public void testGetWithSshPrivateKey() throws JSchException {
    String privateKey = readPrivateKeyFromFile();
    applyAuthentication(new SshPrivateKeyAuthentication("scmadmin", privateKey));
    ScmInformation information = createInformation("ssh://scm@scm-manager.org:8889/repo/ns/one");
    ScmV2SshNotifier notifier = provider.get(run, information).get();

    assertEquals("ns", notifier.getNamespaceAndName().getNamespace());
    assertEquals("one", notifier.getNamespaceAndName().getName());
  }

  @Test
  public void testGetWithSshUsernamePassword() throws JSchException {
    applyAuthentication(new SshUsernamePasswordAuthentication(any(), any()));
    ScmInformation information = createInformation("ssh://scm@scm-manager.org:8889/repo/ns/one");
    ScmV2SshNotifier notifier = provider.get(run, information).get();

    assertEquals("ns", notifier.getNamespaceAndName().getNamespace());
    assertEquals("one", notifier.getNamespaceAndName().getName());
  }

  private ScmInformation createInformation(String s) {
    return new ScmInformation("sample", s, "abc", "one");
  }

  private void applyAuthentication(SSHAuthentication sshAuthentication) {
    when(authenticationFactory.createSSH(run, "one")).thenReturn(sshAuthentication);
  }

  private static String readPrivateKeyFromFile() {
    StringBuilder contentBuilder = new StringBuilder();
    try (Stream<String> stream = Files.lines(Paths.get(Resources.getResource("ssh/privateKey/scmadmin").toURI()), StandardCharsets.UTF_8)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
    }
    return contentBuilder.toString();
  }

}
