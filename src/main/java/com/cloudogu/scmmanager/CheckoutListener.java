package com.cloudogu.scmmanager;

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

@Extension
public class CheckoutListener extends SCMListener {

  private NotificationService notificationService;

  @Inject
  public void setNotificationService(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Override
  public void onCheckout(Run<?, ?> build, SCM scm, FilePath workspace, TaskListener listener, @CheckForNull File changelogFile, @CheckForNull SCMRevisionState pollingBaseline) {
    notificationService.notify(build, null);
  }
}
