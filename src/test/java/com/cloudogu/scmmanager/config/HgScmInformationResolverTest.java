package com.cloudogu.scmmanager.config;

import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.mercurial.MercurialSCM;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HgScmInformationResolverTest {

  @Mock
  private MercurialSCM hg;

  @Mock
  private Run<?, ?> run;

  private final HgScmInformationResolver resolver = new HgScmInformationResolver();

  @Test
  public void testResolveWithWrongSCM() throws IOException {
    GitSCM git = Mockito.mock(GitSCM.class);

    Collection<ScmInformation> information = resolver.resolve(run, git);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolveWithoutSource() throws IOException {
    applyRevision("abc42");

    Collection<ScmInformation> information = resolver.resolve(run, hg);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolveWithoutRevision() throws IOException {
    when(hg.getSource()).thenReturn("https://scm.scm-manager.org/repo/ns/one");

    Collection<ScmInformation> information = resolver.resolve(run, hg);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolve() throws IOException {
    when(hg.getSource()).thenReturn("https://scm.scm-manager.org/repo/ns/one");
    applyRevision("42abc");
    when(hg.getCredentialsId()).thenReturn("scm-one");

    Collection<ScmInformation> information = resolver.resolve(run, hg);
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
