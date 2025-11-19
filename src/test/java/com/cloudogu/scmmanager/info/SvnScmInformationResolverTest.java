package com.cloudogu.scmmanager.info;

import static com.cloudogu.scmmanager.info.SourceUtilTestHelper.mockSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.scm.SubversionSCM;
import java.util.Collection;
import java.util.Iterator;
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
class SvnScmInformationResolverTest {

    @Mock
    private SubversionSCM svn;

    @Mock
    private Run<TestJob, TestRun> run;

    private final SvnScmInformationResolver resolver = new SvnScmInformationResolver();

    @Test
    void testResolveWithWrongSCM() {
        GitSCM git = Mockito.mock(GitSCM.class);

        Collection<JobInformation> information = resolver.resolve(run, git);
        assertTrue(information.isEmpty());
    }

    @Test
    void testResolveWithoutLocations() {
        Collection<JobInformation> information = resolver.resolve(run, svn);
        assertTrue(information.isEmpty());
    }

    @Test
    void testResolveWithEmptyLocations() {
        mockSource(run, "https://scm.scm-manager.org/repo/ns/one");
        when(svn.getLocations()).thenReturn(new SubversionSCM.ModuleLocation[0]);

        Collection<JobInformation> information = resolver.resolve(run, svn);
        assertTrue(information.isEmpty());
    }

    @Test
    void testResolveWithOneLocation() {
        mockSource(run, "https://scm.scm-manager.org/repo/ns/one");
        applyLocations(location("https://scm.scm-manager.org/repo/ns/one", "scm-one"));
        applyRevisions(42);

        Collection<JobInformation> information = resolver.resolve(run, svn);
        assertEquals(1, information.size());

        JobInformationAssertions.info(
                information.iterator().next(), "svn", "42", "https://scm.scm-manager.org/repo/ns/one", "scm-one");
    }

    @Test
    void testResolveOneWithoutRevision() {
        mockSource(run, "https://scm.scm-manager.org/repo/ns/one");
        applyLocations(location("https://scm.scm-manager.org/repo/ns/one", "scm-one"));

        Collection<JobInformation> information = resolver.resolve(run, svn);
        assertTrue(information.isEmpty());
    }

    @Test
    void testResolveMultipleWithTooFewRevision() {
        mockSource(
                run,
                "https://scm.scm-manager.org/repo/ns/one",
                "https://scm.scm-manager.org/repo/ns/two",
                "https://scm.scm-manager.org/repo/ns/three");
        applyLocations(
                location("https://scm.scm-manager.org/repo/ns/one", "scm-one"),
                location("https://scm.scm-manager.org/repo/ns/two", "scm-two"),
                location("https://scm.scm-manager.org/repo/ns/three", "scm-three"));
        applyRevisions(42, 21);

        Collection<JobInformation> information = resolver.resolve(run, svn);
        assertEquals(2, information.size());
    }

    @Test
    void testResolveMultipleWithoutSourceOwner() {
        applyLocations(
                location("https://scm.scm-manager.org/repo/ns/one", "scm-one"),
                location("https://scm.scm-manager.org/repo/ns/two", "scm-two"));
        applyRevisions(42, 21);

        Collection<JobInformation> information = resolver.resolve(run, svn);
        assertEquals(2, information.size());
    }

    @Test
    void testResolveWithMultipleLocations() {
        mockSource(run, "https://scm.scm-manager.org/repo/ns/one", "https://scm.scm-manager.org:443/repo/ns/two");
        applyLocations(
                location("https://scm.scm-manager.org/repo/ns/one", "scm-one"),
                location("https://scm.scm-manager.org/repo/ns/two", "scm-two"));
        applyRevisions(42, 21);

        Collection<JobInformation> information = resolver.resolve(run, svn);
        assertEquals(2, information.size());

        Iterator<JobInformation> iterator = information.iterator();
        JobInformationAssertions.info(
                iterator.next(), "svn", "42", "https://scm.scm-manager.org/repo/ns/one", "scm-one");
        JobInformationAssertions.info(
                iterator.next(), "svn", "21", "https://scm.scm-manager.org/repo/ns/two", "scm-two");
    }

    @SuppressWarnings("unchecked")
    private void applyRevisions(int... revs) {
        doAnswer(ic -> {
                    Map<String, String> env = ic.getArgument(1);
                    if (revs.length == 1) {
                        env.put("SVN_REVISION", String.valueOf(revs[0]));
                    } else {
                        for (int i = 0; i < revs.length; i++) {
                            env.put("SVN_REVISION_" + (i + 1), String.valueOf(revs[i]));
                        }
                    }
                    return null;
                })
                .when(svn)
                .buildEnvironment(any(Run.class), any(Map.class));
    }

    private void applyLocations(SubversionSCM.ModuleLocation... locations) {
        when(svn.getLocations()).thenReturn(locations);
    }

    private SubversionSCM.ModuleLocation location(String remote, String credentialsId) {
        return new SubversionSCM.ModuleLocation(remote, credentialsId, null, null, true, true);
    }
}
