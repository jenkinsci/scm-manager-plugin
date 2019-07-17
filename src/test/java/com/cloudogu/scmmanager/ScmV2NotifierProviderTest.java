package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.config.ScmInformation;
import hudson.model.Run;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmV2NotifierProviderTest {

  @Mock
  private AuthenticationFactory authenticationFactory;

  @InjectMocks
  private ScmV2NotifierProvider provider;

  @Mock
  private Run<?, ?> run;

  @Test
  public void testGetWithoutMatchingNotifier() throws MalformedURLException {
    ScmInformation information = createInformation("sample://one");
    Optional<ScmV2Notifier> notifier = provider.get(run, information);
    assertFalse(notifier.isPresent());
  }

  @Test
  public void testGet() throws MalformedURLException {
    applyAuthentication();

    ScmInformation information = createInformation("https://scm.scm-manager.org/repo/ns/one");
    ScmV2Notifier notifier = provider.get(run, information).get();

    assertEquals("ns", notifier.getNamespaceAndName().getNamespace());
    assertEquals("one", notifier.getNamespaceAndName().getName());
    assertEquals("https://scm.scm-manager.org", notifier.getInstance().toExternalForm());
    assertSame(AuthenticationFactory.NOOP_AUTHENTICATION, notifier.getAuthentication());
  }

  @Test
  public void testGetWithContextPath() throws MalformedURLException {
    applyAuthentication();

    ScmInformation information = createInformation("https://scm.scm-manager.org/scm/repo/ns/one");
    ScmV2Notifier notifier = provider.get(run, information).get();

    assertEquals("https://scm.scm-manager.org/scm", notifier.getInstance().toExternalForm());
  }

  @Test
  public void testGetWithPort() throws MalformedURLException {
    applyAuthentication();

    ScmInformation information = createInformation("http://localhost:8080/scm/repo/ns/one");
    ScmV2Notifier notifier = provider.get(run, information).get();

    assertEquals("http://localhost:8080/scm", notifier.getInstance().toExternalForm());
  }

  private ScmInformation createInformation(String s) {
    return new ScmInformation("sample", s, "abc", "one");
  }

  private void applyAuthentication() {
    when(authenticationFactory.create(run, "one")).thenReturn(AuthenticationFactory.NOOP_AUTHENTICATION);
  }

}
