package com.cloudogu.scmmanager.info;

import com.google.common.base.Strings;
import hudson.model.Run;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.plugins.mercurial.MercurialSCMSource;
import hudson.scm.SCM;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HgScmInformationResolver implements ScmInformationResolver {

    private static final Logger LOG = LoggerFactory.getLogger(HgScmInformationResolver.class);

    private static final String TYPE = "hg";

    @Override
    public Collection<JobInformation> resolve(Run<?, ?> run, SCM scm) {
        if (!(scm instanceof MercurialSCM)) {
            LOG.trace("scm is not a mercurial scm, skip collecting information");
            return Collections.emptyList();
        }

        MercurialSCM hg = (MercurialSCM) scm;

        String source = hg.getSource();
        if (Strings.isNullOrEmpty(source)) {
            LOG.warn("scm has no source, skip collecting information");
            return Collections.emptyList();
        }

        String revision = getRevision(hg, run);
        if (Strings.isNullOrEmpty(revision)) {
            LOG.warn("scm has no revision, skip collecting information");
            return Collections.emptyList();
        }

        if (!SourceUtil.extractSourceOwner(run).isPresent()) {
            LOG.trace("run does not contain source owner, start collecting information");
            return Collections.singleton(createInformation(hg, revision, source));
        }

        Collection<String> remoteBases =
                SourceUtil.getSources(run, MercurialSCMSource.class, MercurialSCMSource::getSource);

        if (remoteBases.isEmpty()) {
            LOG.trace("source owner has no sources, skip collecting information");
            return Collections.emptyList();
        }

        JobInformation config = createInformation(hg, revision, source);

        if (remoteBases.contains(URIs.normalize(config.getUrl()))) {
            return Collections.singleton(config);
        }

        return Collections.emptyList();
    }

    private JobInformation createInformation(MercurialSCM hg, String revision, String source) {
        return new JobInformation(TYPE, source, revision, hg.getCredentialsId(), false);
    }

    private String getRevision(MercurialSCM scm, Run<?, ?> run) {
        Map<String, String> env = new HashMap<>();
        scm.buildEnvironment(run, env);
        return env.get("MERCURIAL_REVISION");
    }
}
