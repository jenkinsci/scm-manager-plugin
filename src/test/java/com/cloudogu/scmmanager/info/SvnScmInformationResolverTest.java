package com.cloudogu.scmmanager.info;

import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.scm.SubversionSCM;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static com.cloudogu.scmmanager.info.SourceUtilTestHelper.mockSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SvnScmInformationResolverTest {

  @Mock
  private SubversionSCM svn;

  @Mock
  private Run<TestJob, TestRun> run;

  private final SvnScmInformationResolver resolver = new SvnScmInformationResolver();

  @Test
  public void testResolveWithWrongSCM() {
    GitSCM git = Mockito.mock(GitSCM.class);

    Collection<JobInformation> information = resolver.resolve(run, git);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolveWithoutLocations() {
    Collection<JobInformation> information = resolver.resolve(run, svn);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolveWithEmptyLocations() {
    mockSource(run, "https://scm.scm-manager.org/repo/ns/one");
    when(svn.getLocations()).thenReturn(new SubversionSCM.ModuleLocation[0]);

    Collection<JobInformation> information = resolver.resolve(run, svn);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolveWithOneLocation() {
    mockSource(run, "https://scm.scm-manager.org/repo/ns/one");
    applyLocations(location("https://scm.scm-manager.org/repo/ns/one", "scm-one"));
    applyRevisions(42);

    Collection<JobInformation> information = resolver.resolve(run, svn);
    assertEquals(1, information.size());

    Assertions.info(
      information.iterator().next(),
      "svn",
      "42",
      "https://scm.scm-manager.org/repo/ns/one",
      "scm-one"
    );
  }

  @Test
  public void testResolveOneWithoutRevision() {
    mockSource(run, "https://scm.scm-manager.org/repo/ns/one");
    applyLocations(location("https://scm.scm-manager.org/repo/ns/one", "scm-one"));

    Collection<JobInformation> information = resolver.resolve(run, svn);
    assertTrue(information.isEmpty());
  }

  @Test
  public void testResolveMutlipleWithTooFewRevision() {
    mockSource(
      run,
      "https://scm.scm-manager.org/repo/ns/one",
      "https://scm.scm-manager.org/repo/ns/two",
      "https://scm.scm-manager.org/repo/ns/three");
    applyLocations(
      location("https://scm.scm-manager.org/repo/ns/one", "scm-one"),
      location("https://scm.scm-manager.org/repo/ns/two", "scm-two"),
      location("https://scm.scm-manager.org/repo/ns/three", "scm-three")
    );
    applyRevisions(42, 21);

    Collection<JobInformation> information = resolver.resolve(run, svn);
    assertEquals(2, information.size());
  }

  @Test
  public void testResolveWithMultipleLocations() {
    mockSource(
      run,
      "https://scm.scm-manager.org/repo/ns/one",
      "https://scm.scm-manager.org/repo/ns/two");
    applyLocations(
      location("https://scm.scm-manager.org/repo/ns/one", "scm-one"),
      location("https://scm.scm-manager.org/repo/ns/two", "scm-two")
    );
    applyRevisions(42, 21);

    Collection<JobInformation> information = resolver.resolve(run, svn);
    assertEquals(2, information.size());

    Iterator<JobInformation> iterator = information.iterator();
    Assertions.info(
      iterator.next(),
      "svn",
      "42",
      "https://scm.scm-manager.org/repo/ns/one",
      "scm-one"
    );
    Assertions.info(
      iterator.next(),
      "svn",
      "21",
      "https://scm.scm-manager.org/repo/ns/two",
      "scm-two"
    );
  }

  @SuppressWarnings("unchecked")
  private void applyRevisions(int... revs) {
    doAnswer(ic -> {
      Map<String, String> env = ic.getArgument(1);
      if (revs.length == 1) {
        env.put("SVN_REVISION", String.valueOf(revs[0]));
      } else {
        for (int i=0; i<revs.length; i++) {
          env.put("SVN_REVISION_" + (i + 1), String.valueOf(revs[i]));
        }
      }
      return null;
    }).when(svn).buildEnvironment(any(Run.class), any(Map.class));
  }

  private void applyLocations(SubversionSCM.ModuleLocation... locations) {
    when(svn.getLocations()).thenReturn(locations);
  }

  private SubversionSCM.ModuleLocation location(String remote, String credentialsId) {
    return new SubversionSCM.ModuleLocation(
      remote,
      credentialsId,
      null,
      null,
      true,
      true
    );
  }

}
