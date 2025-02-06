package com.cloudogu.scmmanager.info;

import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.SCM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScmInformationService {

    private static final Logger LOG = LoggerFactory.getLogger(ScmInformationService.class);

    /**
     * Use {@link #resolve(Run, SCM)} instead.
     *
     * @param run the build job
     * @return list of resolved job information
     * @deprecated use {@link #resolve(Run, SCM)} instead.
     */
    @Deprecated
    public List<JobInformation> resolve(Run<?, ?> run) {
        LOG.trace("resolve job information from run {}", run);
        Job<?, ?> job = run.getParent();
        return collect(((configurations, resolver) -> configurations.addAll(resolver.resolve(run, job))));
    }

    public List<JobInformation> resolve(Run<?, ?> run, SCM scm) {
        LOG.trace("resolve job information from run {} and scm {}", run, scm);
        return collect(((configurations, resolver) -> configurations.addAll(resolver.resolve(run, scm))));
    }

    private List<JobInformation> collect(Collector collector) {
        List<JobInformation> configurations = new ArrayList<>();
        for (JobInformationResolverProvider provider : JobInformationResolverProvider.all()) {
            Optional<JobInformationResolver> resolver = provider.get();
            resolver.ifPresent(jobInformationResolver -> collector.collect(configurations, jobInformationResolver));
        }
        return configurations;
    }

    @FunctionalInterface
    public interface Collector {
        void collect(List<JobInformation> configurations, JobInformationResolver resolver);
    }
}
