package com.cloudogu.scmmanager;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;
import javax.inject.Inject;

@Extension
public class CompletionListener extends RunListener<Run<?, ?>> {

    private NotificationService notificationService;

    @Inject
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onCompleted(Run<?, ?> run, @Nonnull TaskListener listener) {
        notificationService.notify(run, run.getResult());
    }
}
