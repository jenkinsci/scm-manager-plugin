package com.cloudogu.scmmanager.info;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import hudson.model.Job;
import hudson.model.Run;
import java.util.Collection;
import jenkins.triggers.SCMTriggerItem;

public interface ScmInformationResolver extends JobInformationResolver {

    @Override
    default Collection<JobInformation> resolve(Run<?, ?> run, Job<?, ?> job) {
        SCMTriggerItem trigger = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);
        if (trigger != null) {
            return trigger.getSCMs().stream()
                    .flatMap(scm -> resolve(run, scm).stream())
                    .collect(toList());
        }
        return emptyList();
    }
}
