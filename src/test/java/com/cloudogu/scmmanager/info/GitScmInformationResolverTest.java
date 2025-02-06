package com.cloudogu.scmmanager.info;

import static com.cloudogu.scmmanager.info.SourceUtilTestHelper.mockSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.Revision;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.util.BuildData;
import hudson.plugins.mercurial.MercurialSCM;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GitScmInformationResolverTest {

    @Mock
    private GitSCM git;

    @Mock
    private Run<TestJob, TestRun> run;

    private final GitScmInformationResolver resolver = new GitScmInformationResolver();

    @Test
    public void testResolveNonGitSCM() {
        MercurialSCM scm = mock(MercurialSCM.class);
        Collection<JobInformation> information = resolver.resolve(run, scm);
        assertTrue(information.isEmpty());
    }

    @Test
    public void testResolveWithoutBuildData() {
        Collection<JobInformation> information = resolver.resolve(run, git);
        assertTrue(information.isEmpty());
    }

    @Test
    public void testResolveWithoutRevision() {
        BuildData buildData = mock(BuildData.class);
        when(git.getBuildData(run)).thenReturn(buildData);
        mockSource(run, "https://scm.scm-manager.org/repo/ns/one");

        Collection<JobInformation> information = resolver.resolve(run, git);
        assertTrue(information.isEmpty());
    }

    @Test
    public void testResolveWithoutSha1() {
        mockSource(run, "https://scm.scm-manager.org/repo/ns/one");
        BuildData buildData = mock(BuildData.class);
        Revision revision = mock(Revision.class);
        when(buildData.getLastBuiltRevision()).thenReturn(revision);
        when(git.getBuildData(run)).thenReturn(buildData);

        Collection<JobInformation> information = resolver.resolve(run, git);
        assertTrue(information.isEmpty());
    }

    @Test
    public void testResolveWithoutUserRepositoryData() {
        applyRevision("abc42");

        Collection<JobInformation> information = resolver.resolve(run, git);
        assertTrue(information.isEmpty());
    }

    @Test
    public void testResolve() {
        mockSource(run, "https://scm.scm-manager.org/repo/ns/one", "https://scm.scm-manager.org/repo/ns/two");
        applyRevision("abc42");
        applyUrcs(
                urc("https://scm.scm-manager.org/repo/ns/one", "scm-one"),
                urc("https://scm.scm-manager.org:443/repo/ns/two", "scm-two"));

        Collection<JobInformation> information = resolver.resolve(run, git);
        assertEquals(2, information.size());

        Iterator<JobInformation> it = information.iterator();
        Assertions.info(it.next(), "git", "abc42", "https://scm.scm-manager.org/repo/ns/one", "scm-one");
        Assertions.info(it.next(), "git", "abc42", "https://scm.scm-manager.org:443/repo/ns/two", "scm-two");
    }

    @Test
    public void testResolveWithoutSourceOwner() {
        applyRevision("abc42");
        applyUrcs(
                urc("https://scm.scm-manager.org/repo/ns/one", "scm-one"),
                urc("https://scm.scm-manager.org/repo/ns/two", "scm-two"));

        Collection<JobInformation> information = resolver.resolve(run, git);
        assertEquals(2, information.size());

        Iterator<JobInformation> it = information.iterator();
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
