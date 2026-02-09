package com.cloudogu.scmmanager.info;

import static com.cloudogu.scmmanager.info.SourceUtilTestHelper.mockSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GitScmInformationResolverTest {

    @Mock
    private GitSCM git;

    @Mock
    private Run<TestJob, TestRun> run;

    private final GitScmInformationResolver resolver = new GitScmInformationResolver();

    @Test
    void testResolveNonGitSCM() {
        MercurialSCM scm = mock(MercurialSCM.class);
        Collection<JobInformation> information = resolver.resolve(run, scm);
        assertTrue(information.isEmpty());
    }

    @Test
    void testResolveWithoutBuildData() {
        Collection<JobInformation> information = resolver.resolve(run, git);
        assertTrue(information.isEmpty());
    }

    @Test
    void testResolveWithoutRevision() {
        BuildData buildData = mock(BuildData.class);
        when(git.getBuildData(run)).thenReturn(buildData);
        mockSource(run, "https://scm.scm-manager.org/repo/ns/one");

        Collection<JobInformation> information = resolver.resolve(run, git);
        assertTrue(information.isEmpty());
    }

    @Test
    void testResolveWithoutSha1() {
        mockSource(run, "https://scm.scm-manager.org/repo/ns/one");
        BuildData buildData = mock(BuildData.class);
        Revision revision = mock(Revision.class);
        when(buildData.getLastBuiltRevision()).thenReturn(revision);
        when(git.getBuildData(run)).thenReturn(buildData);

        Collection<JobInformation> information = resolver.resolve(run, git);
        assertTrue(information.isEmpty());
    }

    @Test
    void testResolveWithoutUserRepositoryData() {
        applyRevision("abc42");

        Collection<JobInformation> information = resolver.resolve(run, git);
        assertTrue(information.isEmpty());
    }

    @Test
    void testResolve() {
        mockSource(run, "https://scm.scm-manager.org/repo/ns/one", "https://scm.scm-manager.org/repo/ns/two");
        applyRevision("abc42");
        applyUrcs(
                urc("https://scm.scm-manager.org/repo/ns/one", "scm-one"),
                urc("https://scm.scm-manager.org:443/repo/ns/two", "scm-two"));

        Collection<JobInformation> information = resolver.resolve(run, git);
        assertEquals(2, information.size());

        Iterator<JobInformation> it = information.iterator();
        JobInformationAssertions.info(it.next(), "git", "abc42", "https://scm.scm-manager.org/repo/ns/one", "scm-one");
        JobInformationAssertions.info(
                it.next(), "git", "abc42", "https://scm.scm-manager.org:443/repo/ns/two", "scm-two");
    }

    @Test
    void testResolveWithoutSourceOwner() {
        applyRevision("abc42");
        applyUrcs(
                urc("https://scm.scm-manager.org/repo/ns/one", "scm-one"),
                urc("https://scm.scm-manager.org/repo/ns/two", "scm-two"));

        Collection<JobInformation> information = resolver.resolve(run, git);
        assertEquals(2, information.size());

        Iterator<JobInformation> it = information.iterator();
        JobInformationAssertions.info(it.next(), "git", "abc42", "https://scm.scm-manager.org/repo/ns/one", "scm-one");
        JobInformationAssertions.info(it.next(), "git", "abc42", "https://scm.scm-manager.org/repo/ns/two", "scm-two");
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
