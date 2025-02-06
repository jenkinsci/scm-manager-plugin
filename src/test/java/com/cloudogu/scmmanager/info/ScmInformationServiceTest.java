package com.cloudogu.scmmanager.info;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.ChangeLogParser;
import hudson.scm.SCM;
import jenkins.triggers.SCMTriggerItem;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class ScmInformationServiceTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Mock
    private Run run;

    private ScmInformationService informationService = new ScmInformationService();

    @Test
    public void testResolveWithoutScmResolver() {
        List<JobInformation> informationList = informationService.resolve(run);
        assertTrue(informationList.isEmpty());
    }

    @Test
    public void testResolveWithUnknownSCM() {
        applySCM(new UnknownSCM());

        List<JobInformation> informationList = informationService.resolve(run);
        assertTrue(informationList.isEmpty());
    }

    @Test
    public void testResolve() {
        applySCM(new SampleSCM());

        List<JobInformation> informationList = informationService.resolve(run);
        assertEquals(1, informationList.size());
        assertEquals("sample", informationList.get(0).getType());
    }

    private void applySCM(SCM scm) {
        Job job = mock(Job.class, withSettings().extraInterfaces(SCMTriggerItem.class));
        when(run.getParent()).thenReturn(job);

        SCMTriggerItem triggerItem = (SCMTriggerItem) job;

        Collection scms = Collections.singletonList(scm);
        when(triggerItem.getSCMs()).thenReturn(scms);
    }

    public static class UnknownSCM extends SCM {
        @Override
        public ChangeLogParser createChangeLogParser() {
            return null;
        }
    }

    public static class SampleSCM extends SCM {
        @Override
        public ChangeLogParser createChangeLogParser() {
            return null;
        }
    }

    public static class SampleScmInformationResolver implements ScmInformationResolver {

        @Override
        public Collection<JobInformation> resolve(Run<?, ?> run, SCM scm) {
            if (!(scm instanceof SampleSCM)) {
                return Collections.emptyList();
            }
            return Collections.singletonList(
                new JobInformation("sample", "https://scm.manager.org", "abc", "one", false)
            );
        }
    }

    @Extension
    public static class SampleScmInformationResolverProvider implements JobInformationResolverProvider {
        @Override
        public Optional<JobInformationResolver> get() {
            return Optional.of(new SampleScmInformationResolver());
        }
    }

}
