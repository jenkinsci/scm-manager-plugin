package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.JobInformation;
import com.cloudogu.scmmanager.info.ScmInformationService;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.SCMListener;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import java.io.File;
import java.util.List;

@Extension
public class CheckoutListener extends SCMListener {

  private ScmInformationService informationService;
  private NotificationService notificationService;

  @Inject
  public void setInformationService(ScmInformationService informationService) {
    this.informationService = informationService;
  }

  @Inject
  public void setNotificationService(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Override
  public void onCheckout(Run<?, ?> run, SCM scm, FilePath workspace, TaskListener listener, @CheckForNull File changelogFile, @CheckForNull SCMRevisionState pollingBaseline) {
    List<JobInformation> jobInformation = informationService.resolve(run, scm);
    run.addAction(new NotificationAction(jobInformation));
    notificationService.notify(run, null);
  }
}
