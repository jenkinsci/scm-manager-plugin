package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.config.ScmInformation;
import com.cloudogu.scmmanager.config.ScmInformationService;
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

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest {

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();

  @Mock
  private BuildStatusFactory buildStatusFactory;

  @Mock
  private ScmInformationService informationService;

  @Mock
  private Run<?, ?> run;

  @InjectMocks
  private NotificationService notificationService;

  @Test
  public void testNotify() {
    String rootUrl = jenkins.jenkins.getRootUrl();

    BuildStatus status = BuildStatus.success("scm-manager-plugin", "http://localhost:8080/jenkins/job/scm-manager-plugin/42");
    when(buildStatusFactory.create(rootUrl, run, Result.SUCCESS)).thenReturn(status);

    ScmInformation information = new ScmInformation("git", "sample://scm.scm-manager/repo/ns/core", "abc42", "scm-core");
    when(informationService.resolve(run)).thenReturn(Collections.singletonList(information));
    notificationService.notify(run, Result.SUCCESS);

    CapturingNotifier notifier = getNotifier();
    assertNotNull(notifier);
    assertEquals("abc42", notifier.revision);
    assertEquals(status, notifier.buildStatus);
  }

  @Test
  public void testNotifyWithoutInformation() {
    when(informationService.resolve(run)).thenReturn(Collections.emptyList());
    notificationService.notify(run, Result.SUCCESS);
  }

  @Test
  public void testNotifyWithoutBuildStatus() {
    ScmInformation information = new ScmInformation("git", "sample://scm.scm-manager/repo/ns/core", "abc42", "scm-core");
    when(informationService.resolve(run)).thenReturn(Collections.singletonList(information));

    notificationService.notify(run, Result.SUCCESS);
  }

  private CapturingNotifier getNotifier() {
    for (NotifierProvider provider : NotifierProvider.all()) {
      if (provider instanceof SampleNotifierProvider) {
        return ((SampleNotifierProvider)provider).notifier;
      }
    }
    return null;
  }

  @Extension
  public static class SampleNotifierProvider implements NotifierProvider {

    private CapturingNotifier notifier = new CapturingNotifier();

    @Override
    public Optional<? extends Notifier> get(Run<?, ?> run, ScmInformation information) {
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
