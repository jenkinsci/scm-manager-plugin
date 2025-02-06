package com.cloudogu.scmmanager.info;

import com.google.common.base.Strings;
import hudson.model.Run;
import hudson.scm.SCM;
import hudson.scm.SubversionSCM;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jenkins.scm.impl.subversion.SubversionSCMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvnScmInformationResolver implements ScmInformationResolver {

    private static final Logger LOG = LoggerFactory.getLogger(SvnScmInformationResolver.class);

    private static final String TYPE = "svn";

    @Override
    public Collection<JobInformation> resolve(Run<?, ?> run, SCM scm) {
        if (!(scm instanceof SubversionSCM)) {
            LOG.trace("scm is not a svn repository, skip collecting information");
            return Collections.emptyList();
        }

        SubversionSCM svn = (SubversionSCM) scm;

        Map<String, String> env = new HashMap<>();
        svn.buildEnvironment(run, env);

        List<JobInformation> configurations = new ArrayList<>();
        SubversionSCM.ModuleLocation[] locations = svn.getLocations();
        if (locations != null) {
            appendInformation(configurations, locations, env);
        }

        if (configurations.isEmpty()) {
            LOG.trace("svn scm does not contain valid job information");
            return Collections.emptyList();
        }

        if (!SourceUtil.extractSourceOwner(run).isPresent()) {
            LOG.trace("run does not contain source owner, start collecting information");
            return configurations;
        }

        Collection<String> remoteBases =
                SourceUtil.getSources(run, SubversionSCMSource.class, SubversionSCMSource::getRemoteBase);

        if (remoteBases.isEmpty()) {
            LOG.trace("source owner has no sources, skip collecting information");
            return Collections.emptyList();
        }

        return configurations.stream()
                .filter(jobInformation -> remoteBases.stream().anyMatch(remoteBase -> {
                    boolean valid = URIs.normalize(jobInformation.getUrl()).startsWith(remoteBase);
                    if (!valid) {
                        LOG.trace(
                                "skip {}, because it does not start of the source owner {}. Maybe it is a library.",
                                jobInformation.getUrl(),
                                remoteBases);
                    }
                    return valid;
                }))
                .collect(Collectors.toList());
    }

    private void appendInformation(
            List<JobInformation> configurations, SubversionSCM.ModuleLocation[] locations, Map<String, String> env) {
        if (locations.length == 1) {
            appendInformation(configurations, locations[0], env.get("SVN_REVISION"));
        } else if (locations.length > 1) {
            appendMultipleInformation(configurations, locations, env);
        }
    }

    private void appendMultipleInformation(
            List<JobInformation> configurations, SubversionSCM.ModuleLocation[] locations, Map<String, String> env) {
        for (int i = 0; i < locations.length; i++) {
            appendInformation(configurations, locations[i], env.get("SVN_REVISION_" + (i + 1)));
        }
    }

    private void appendInformation(
            List<JobInformation> configurations, SubversionSCM.ModuleLocation location, String revision) {
        String url = location.getURL();
        if (Strings.isNullOrEmpty(url)) {
            LOG.trace("svn location does not contain url");
            return;
        }

        if (Strings.isNullOrEmpty(revision)) {
            LOG.trace("svn location does not contain revision");
            return;
        }

        configurations.add(new JobInformation(TYPE, url, revision, location.credentialsId, false));
    }
}
