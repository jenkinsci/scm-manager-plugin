package com.cloudogu.scmmanager.info;

import java.util.Optional;
import jenkins.model.Jenkins;

abstract class AbstractScmInformationResolverProvider implements JobInformationResolverProvider {

    private final String plugin;

    AbstractScmInformationResolverProvider(String plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<JobInformationResolver> get() {
        if (Jenkins.get().getPlugin(plugin) != null) {
            return Optional.of(create());
        }
        return Optional.empty();
    }

    public abstract ScmInformationResolver create();
}
