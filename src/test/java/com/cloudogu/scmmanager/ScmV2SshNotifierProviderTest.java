package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.ScmInformation;
import hudson.model.Run;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
  public void testGetWithoutMatchingNotifier() {
    ScmInformation information = createInformation("sample://one");
    Optional<ScmV2SshNotifier> notifier = provider.get(run, information);
    assertFalse(notifier.isPresent());
  }

  @Test
  public void testCreateNotifier() {
    applyAuthentication(new SSHAuthentication(null));
    ScmInformation information = createInformation("ssh://scm@scm-manager.org:8889/repo/ns/one");
    ScmV2SshNotifier notifier = provider.get(run, information).get();

    assertEquals("ns", notifier.getNamespaceAndName().getNamespace());
    assertEquals("one", notifier.getNamespaceAndName().getName());
    assertEquals(notifier.getConnection().getHostname(), "scm-manager.org");
    assertEquals(notifier.getConnection().getPort(), 8889);
  }


  private ScmInformation createInformation(String s) {
    return new ScmInformation("sample", s, "abc", "one");
  }

  private void applyAuthentication(SSHAuthentication sshAuthentication) {
    when(authenticationFactory.createSSH(run, "one")).thenReturn(sshAuthentication);
  }
}
