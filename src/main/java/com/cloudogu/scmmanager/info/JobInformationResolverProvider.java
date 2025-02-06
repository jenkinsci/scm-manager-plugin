package com.cloudogu.scmmanager.info;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.Optional;
import jenkins.model.Jenkins;

public interface JobInformationResolverProvider extends ExtensionPoint {

    Optional<JobInformationResolver> get();

    static ExtensionList<JobInformationResolverProvider> all() {
        return Jenkins.get().getExtensionList(JobInformationResolverProvider.class);
    }
}
