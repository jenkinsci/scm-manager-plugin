package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.JobInformation;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.Action;

import java.util.List;

public class NotificationAction implements Action {

    private final List<JobInformation> jobInformation;

    NotificationAction(List<JobInformation> jobInformation) {
        this.jobInformation = jobInformation;
    }

    public List<JobInformation> getJobInformation() {
        return jobInformation;
    }

    @Nullable
    @Override
    public String getIconFileName() {
        return null;
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public String getUrlName() {
        return null;
    }
}
