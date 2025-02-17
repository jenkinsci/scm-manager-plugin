package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.JobInformation;
import com.google.common.base.Strings;
import hudson.model.Result;
import hudson.model.Run;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        Collection<NotificationAction> actions = run.getActions(NotificationAction.class);
        if (actions == null || actions.isEmpty()) {
            LOG.info("no notification action is attached to build {}", run);
            return;
        }

        List<JobInformation> informationList = actions.stream()
                .map(NotificationAction::getJobInformation)
                .flatMap(List::stream)
                .collect(Collectors.toList());
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
