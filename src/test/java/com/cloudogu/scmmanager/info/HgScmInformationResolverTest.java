package com.cloudogu.scmmanager.info;

import static com.cloudogu.scmmanager.info.SourceUtilTestHelper.mockSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.mercurial.MercurialSCM;
import java.util.Collection;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HgScmInformationResolverTest {

    @Mock
    private MercurialSCM hg;

    @Mock
    private Run<TestJob, TestRun> run;

    private final HgScmInformationResolver resolver = new HgScmInformationResolver();

    @Test
    void testResolveWithWrongSCM() {
        GitSCM git = Mockito.mock(GitSCM.class);

        Collection<JobInformation> information = resolver.resolve(run, git);
        assertTrue(information.isEmpty());
    }

    @Test
    void testResolveWithoutSource() {
        applyRevision("abc42");

        Collection<JobInformation> information = resolver.resolve(run, hg);
        assertTrue(information.isEmpty());
    }

    @Test
    void testResolveWithoutRevision() {
        mockSource(run, "https://scm.scm-manager.org/repo/ns/one");

        Collection<JobInformation> information = resolver.resolve(run, hg);
        assertTrue(information.isEmpty());
    }

    @Test
    void testResolve() {
        mockSource(run, "https://scm.scm-manager.org:443/repo/ns/one");
        doReturn("https://scm.scm-manager.org/repo/ns/one").when(hg).getSource();
        applyRevision("42abc");
        when(hg.getCredentialsId()).thenReturn("scm-one");

        Collection<JobInformation> information = resolver.resolve(run, hg);
        assertEquals(1, information.size());
        JobInformationAssertions.info(
                information.iterator().next(), "hg", "42abc", "https://scm.scm-manager.org/repo/ns/one", "scm-one");
    }

    @Test
    void testResolveWithoutSourceOwner() {
        doReturn("https://scm.scm-manager.org/repo/ns/one").when(hg).getSource();
        applyRevision("42abc");
        when(hg.getCredentialsId()).thenReturn("scm-one");

        Collection<JobInformation> information = resolver.resolve(run, hg);
        assertEquals(1, information.size());
        JobInformationAssertions.info(
                information.iterator().next(), "hg", "42abc", "https://scm.scm-manager.org/repo/ns/one", "scm-one");
    }

    @SuppressWarnings("unchecked")
    private void applyRevision(String revision) {
        doAnswer((ic) -> {
                    Map<String, String> env = ic.getArgument(1);
                    env.put("MERCURIAL_REVISION", revision);
                    return null;
                })
                .when(hg)
                .buildEnvironment(any(Run.class), any(Map.class));
    }
}
