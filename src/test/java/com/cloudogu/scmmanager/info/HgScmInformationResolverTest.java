package com.cloudogu.scmmanager.info;

import com.cloudogu.scmmanager.scm.ScmManagerSource;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.plugins.git.GitSCM;
import hudson.plugins.mercurial.MercurialSCM;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.reactor.ReactorException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HgScmInformationResolverTest {

  @Mock
  private MercurialSCM hg;

  @Mock
  private Run<TestJob, TestRun> run;

  @Mock
  private TestJob job;
  @Mock
  private TestSCMSourceOwner sourceOwner;
  @Mock
  private ScmManagerSource scmSource;

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
    mockSource();

    Collection<JobInformation> information = resolver.resolve(run, hg);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolve() {
    mockSource();
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

  private void mockSource() {
    doReturn(job).when(run).getParent();
    doReturn(sourceOwner).when(job).getParent();
    doReturn(Collections.singletonList(scmSource)).when(sourceOwner).getSCMSources();
    doReturn("https://scm.scm-manager.org/repo/ns/one").when(scmSource).getRemoteUrl();

    doReturn("https://scm.scm-manager.org/repo/ns/one").when(hg).getSource();
  }

  @SuppressWarnings("unchecked")
  private void applyRevision(String revision) {
    doAnswer((ic) -> {
      Map<String, String> env = ic.getArgument(1);
      env.put("MERCURIAL_REVISION", revision);
      return null;
    }).when(hg).buildEnvironment(any(Run.class), any(Map.class));
  }

  private static abstract class TestRun extends Run<TestJob, TestRun> {
    protected TestRun(@Nonnull TestJob job) throws IOException {
      super(job);
    }
  }

  private static abstract class TestJob extends Job<TestJob, TestRun> {
    protected TestJob(ItemGroup parent, String name) {
      super(parent, name);
    }
  }

  private static abstract class TestSCMSourceOwner extends Jenkins implements SCMSourceOwner {
    protected TestSCMSourceOwner(File root, ServletContext context) throws IOException, InterruptedException, ReactorException {
      super(root, context);
    }
  }
}
