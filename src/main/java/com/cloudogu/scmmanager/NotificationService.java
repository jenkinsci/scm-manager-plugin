package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.JobInformation;
import com.google.common.base.Strings;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

class NotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  private BuildStatusFactory buildStatusFactory;

  @Inject
  public void setBuildStatusFactory(BuildStatusFactory buildStatusFactory) {
    this.buildStatusFactory = buildStatusFactory;
  }

  void notify(Run<?, ?> run, Result result) {
    String rootUrl = Jenkins.get().getRootUrl();
    if (Strings.isNullOrEmpty(rootUrl)) {
      LOG.warn("jenkins root url is not configured, skipping scm-manager build status notify");
      return;
    }

    NotificationAction action = run.getAction(NotificationAction.class);
    if (action == null) {
      LOG.info("no notification action is attached to build {}", run);
      return;
    }

    List<JobInformation> informationList = action.getJobInformation();
    if (informationList.isEmpty()) {
      LOG.info("no scm information could be extracted from build {}", run);
      return;
    }

    BuildStatus buildStatus = buildStatusFactory.create(rootUrl, run, result);
    if (buildStatus == null) {
      LOG.warn("could not create build status from build {} with result {}", run, result);
      return;
    }

    for (JobInformation info : informationList) {
      try {
        notify(run, buildStatus, info);
      } catch (IOException ex) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("failed to send build status notification", ex);
        } else {
          LOG.info("failed to send build status notification: {}", ex.getMessage());
        }
      }
    }
  }

  private void notify(Run<?, ?> run, BuildStatus buildStatus, JobInformation info) throws IOException {
    for (NotifierProvider provider : NotifierProvider.all()) {
      Optional<? extends Notifier> notifier = provider.get(run, info);
      if (notifier.isPresent()) {
        notifier.get().notify(info.getRevision(), buildStatus);
        break;
      }
    }
  }

}
