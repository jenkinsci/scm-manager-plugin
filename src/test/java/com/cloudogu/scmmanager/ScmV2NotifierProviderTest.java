package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.JobInformation;
import hudson.model.Run;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
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
    JobInformation information = createInformation("sample://one");
    Optional<ScmV2Notifier> notifier = provider.get(run, information);
    assertFalse(notifier.isPresent());
  }

  @Test
  public void testGet() throws MalformedURLException {
    applyAuthentication();

    JobInformation information = createInformation("https://scm.scm-manager.org/repo/ns/one");
    ScmV2Notifier notifier = provider.get(run, information).get();

    assertEquals("ns", notifier.getNamespaceAndName().getNamespace());
    assertEquals("one", notifier.getNamespaceAndName().getName());
    assertEquals("https://scm.scm-manager.org", notifier.getInstance().toExternalForm());
    assertSame(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION, notifier.getHttpAuthentication());
  }

  @Test
  public void testGetWithFurtherPath() throws MalformedURLException {
    applyAuthentication();

    JobInformation information = createInformation("https://scm.scm-manager.org/repo/ns/one/some/file");
    ScmV2Notifier notifier = provider.get(run, information).get();

    assertEquals("ns", notifier.getNamespaceAndName().getNamespace());
    assertEquals("one", notifier.getNamespaceAndName().getName());
    assertEquals("https://scm.scm-manager.org", notifier.getInstance().toExternalForm());
    assertSame(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION, notifier.getHttpAuthentication());
  }

  @Test
  public void testGetWithDotGit() throws MalformedURLException {
    applyAuthentication();

    JobInformation information = createInformation("https://scm.scm-manager.org/repo/ns/one.git");
    ScmV2Notifier notifier = provider.get(run, information).get();

    assertEquals("ns", notifier.getNamespaceAndName().getNamespace());
    assertEquals("one", notifier.getNamespaceAndName().getName());
    assertEquals("https://scm.scm-manager.org", notifier.getInstance().toExternalForm());
    assertSame(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION, notifier.getHttpAuthentication());
  }

  @Test
  public void testGetWithContextPath() throws MalformedURLException {
    applyAuthentication();

    JobInformation information = createInformation("https://scm.scm-manager.org/scm/repo/ns/one");
    ScmV2Notifier notifier = provider.get(run, information).get();

    assertEquals("https://scm.scm-manager.org/scm", notifier.getInstance().toExternalForm());
  }

  @Test
  public void testGetWithPort() throws MalformedURLException {
    applyAuthentication();

    JobInformation information = createInformation("http://localhost:8080/scm/repo/ns/one");
    ScmV2Notifier notifier = provider.get(run, information).get();

    assertEquals("http://localhost:8080/scm", notifier.getInstance().toExternalForm());
  }

  private JobInformation createInformation(String s) {
    return new JobInformation("sample", s, "abc", "one", false);
  }

  private void applyAuthentication() {
    when(authenticationFactory.createHttp(run, "one")).thenReturn(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION);
  }

}
