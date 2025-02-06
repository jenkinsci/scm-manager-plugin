package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.JobInformation;
import hudson.Extension;
import hudson.model.Result;
import hudson.model.Run;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Mock
    private BuildStatusFactory buildStatusFactory;

    @Mock
    private Run<?, ?> run;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    public void testNotify() {
        String rootUrl = jenkins.jenkins.getRootUrl();

        BuildStatus status = BuildStatus.success(
            "scm-manager-plugin",
            "scm-manager-plugin",
            "http://localhost:8080/jenkins/job/scm-manager-plugin/42"
        );
        when(buildStatusFactory.create(rootUrl, run, Result.SUCCESS)).thenReturn(status);

        JobInformation information = new JobInformation("git", "sample://scm.scm-manager/repo/ns/core", "abc42", "scm-core", false);
        mockJobInformation(information);
        notificationService.notify(run, Result.SUCCESS);

        CapturingNotifier notifier = getNotifier();
        assertNotNull(notifier);
        assertEquals("abc42", notifier.revision);
        assertEquals(status, notifier.buildStatus);
    }

    private void mockJobInformation(JobInformation... information) {
        NotificationAction action = new NotificationAction(Arrays.asList(information));
        when(run.getActions(NotificationAction.class)).thenReturn(Collections.singletonList(action));
    }

    @Test
    public void testNotifyWithoutInformation() {
        mockJobInformation();
        notificationService.notify(run, Result.SUCCESS);

        assertNotCalled();
    }

    @Test
    public void testNotifyWithoutBuildStatus() {
        JobInformation information = new JobInformation("git", "sample://scm.scm-manager/repo/ns/core", "abc42", "scm-core", false);
        mockJobInformation(information);

        notificationService.notify(run, Result.SUCCESS);
        assertNotCalled();
    }

    private void assertNotCalled() {
        CapturingNotifier notifier = getNotifier();
        assertNull(notifier.revision);
        assertNull(notifier.buildStatus);
    }

    private CapturingNotifier getNotifier() {
        for (NotifierProvider provider : NotifierProvider.all()) {
            if (provider instanceof SampleNotifierProvider) {
                return ((SampleNotifierProvider) provider).notifier;
            }
        }
        throw new IllegalStateException("could not find CapturingNotifier");
    }

    @Extension
    public static class SampleNotifierProvider implements NotifierProvider {

        private CapturingNotifier notifier = new CapturingNotifier();

        @Override
        public Optional<? extends Notifier> get(Run<?, ?> run, JobInformation information) {
            if (information.getUrl().startsWith("sample://")) {
                return Optional.of(notifier);
            }
            return Optional.empty();
        }
    }

    public static class CapturingNotifier implements Notifier {

        private String revision;
        private BuildStatus buildStatus;

        @Override
        public void notify(String revision, BuildStatus buildStatus) {
            this.revision = revision;
            this.buildStatus = buildStatus;
        }
    }

}
