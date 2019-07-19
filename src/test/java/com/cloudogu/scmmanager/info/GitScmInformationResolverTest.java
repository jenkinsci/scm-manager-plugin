package com.cloudogu.scmmanager.info;

import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.Revision;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.util.BuildData;
import hudson.plugins.mercurial.MercurialSCM;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitScmInformationResolverTest {

  @Mock
  private GitSCM git;

  @Mock
  private Run<?, ?> run;

  private final GitScmInformationResolver resolver = new GitScmInformationResolver();

  @Test
  public void testResolveNonGitSCM() {
    MercurialSCM scm = mock(MercurialSCM.class);
    Collection<ScmInformation> information = resolver.resolve(run, scm);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolveWithoutBuildData() {
    Collection<ScmInformation> information = resolver.resolve(run, git);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolveWithoutRevision() {
    BuildData buildData = mock(BuildData.class);
    when(git.getBuildData(run)).thenReturn(buildData);

    Collection<ScmInformation> information = resolver.resolve(run, git);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolveWithoutSha1() {
    BuildData buildData = mock(BuildData.class);
    Revision revision = mock(Revision.class);
    when(buildData.getLastBuiltRevision()).thenReturn(revision);
    when(git.getBuildData(run)).thenReturn(buildData);

    Collection<ScmInformation> information = resolver.resolve(run, git);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolveWithoutUserRepositoryData() {
    applyRevision("abc42");

    Collection<ScmInformation> information = resolver.resolve(run, git);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolve() {
    applyRevision("abc42");
    applyUrcs(
      urc("https://scm.scm-manager.org/repo/ns/one", "scm-one"),
      urc("https://scm.scm-manager.org/repo/ns/two", "scm-two")
    );

    Collection<ScmInformation> information = resolver.resolve(run, git);
    assertEquals(2, information.size());

    Iterator<ScmInformation> it = information.iterator();
    Assertions.info(it.next(), "git", "abc42", "https://scm.scm-manager.org/repo/ns/one", "scm-one");
    Assertions.info(it.next(), "git", "abc42", "https://scm.scm-manager.org/repo/ns/two", "scm-two");
  }

  private UserRemoteConfig urc(String url, String credentialsId) {
    return new UserRemoteConfig(url, null, null, credentialsId);
  }

  private void applyUrcs(UserRemoteConfig... urcs) {
    when(git.getUserRemoteConfigs()).thenReturn(Arrays.asList(urcs));
  }

  private void applyRevision(String sha1) {
    Revision revision = mock(Revision.class);
    when(revision.getSha1String()).thenReturn(sha1);

    BuildData buildData = mock(BuildData.class);
    when(buildData.getLastBuiltRevision()).thenReturn(revision);

    when(git.getBuildData(run)).thenReturn(buildData);
  }

}
