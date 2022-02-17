package com.cloudogu.scmmanager.info;

import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.mercurial.MercurialSCM;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Map;

import static com.cloudogu.scmmanager.info.SourceUtilTestHelper.mockSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HgScmInformationResolverTest {

  @Mock
  private MercurialSCM hg;

  @Mock
  private Run<TestJob, TestRun> run;

  private final HgScmInformationResolver resolver = new HgScmInformationResolver();

  @Test
  public void testResolveWithWrongSCM() {
    GitSCM git = Mockito.mock(GitSCM.class);

    Collection<JobInformation> information = resolver.resolve(run, git);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolveWithoutSource() {
    applyRevision("abc42");

    Collection<JobInformation> information = resolver.resolve(run, hg);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolveWithoutRevision() {
    mockSource(run, "https://scm.scm-manager.org/repo/ns/one");

    Collection<JobInformation> information = resolver.resolve(run, hg);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolve() {
    mockSource(run, "https://scm.scm-manager.org:443/repo/ns/one");
    doReturn("https://scm.scm-manager.org/repo/ns/one").when(hg).getSource();
    applyRevision("42abc");
    when(hg.getCredentialsId()).thenReturn("scm-one");

    Collection<JobInformation> information = resolver.resolve(run, hg);
    assertEquals(1, information.size());
    Assertions.info(
      information.iterator().next(),
      "hg",
      "42abc",
      "https://scm.scm-manager.org/repo/ns/one",
      "scm-one"
    );
  }

  @Test
  public void testResolveWithoutSourceOwner() {
    doReturn("https://scm.scm-manager.org/repo/ns/one").when(hg).getSource();
    applyRevision("42abc");
    when(hg.getCredentialsId()).thenReturn("scm-one");

    Collection<JobInformation> information = resolver.resolve(run, hg);
    assertEquals(1, information.size());
    Assertions.info(
      information.iterator().next(),
      "hg",
      "42abc",
      "https://scm.scm-manager.org/repo/ns/one",
      "scm-one"
    );
  }

  @SuppressWarnings("unchecked")
  private void applyRevision(String revision) {
    doAnswer((ic) -> {
      Map<String, String> env = ic.getArgument(1);
      env.put("MERCURIAL_REVISION", revision);
      return null;
    }).when(hg).buildEnvironment(any(Run.class), any(Map.class));
  }

}
