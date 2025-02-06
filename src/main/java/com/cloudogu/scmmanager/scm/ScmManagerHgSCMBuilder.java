package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.plugins.mercurial.MercurialSCMBuilder;
import hudson.plugins.mercurial.MercurialSCMSource;
import hudson.plugins.mercurial.browser.HgBrowser;
import java.lang.reflect.Constructor;
import jenkins.scm.api.SCMRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScmManagerHgSCMBuilder extends MercurialSCMBuilder<ScmManagerHgSCMBuilder> {

    private static final Logger LOG = LoggerFactory.getLogger(ScmManagerHgSCMBuilder.class);

    public ScmManagerHgSCMBuilder(@NonNull ScmManagerHead head, SCMRevision revision, String credentialsId) {
        super(head, revision, head.getCloneInformation().getUrl(), credentialsId);

        HgBrowser browser = findAndCreateBrowser(head.getCloneInformation().getUrl());
        if (browser != null) {
            withBrowser(browser);
        }

        if (revision instanceof ScmManagerRevision) {
            withRevision(new MercurialSCMSource.MercurialRevision(head, ((ScmManagerRevision) revision).getRevision()));
        }
    }

    @VisibleForTesting
    static HgBrowser findAndCreateBrowser(String url) {
        return findAndCreateBrowser("hudson.plugins.mercurial.browser.ScmManager", url);
    }

    @VisibleForTesting
    static HgBrowser findAndCreateBrowser(String className, String url) {
        try {
            Class<? extends HgBrowser> clazz = Class.forName(className).asSubclass(HgBrowser.class);
            Constructor<? extends HgBrowser> constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(url);
        } catch (ClassNotFoundException e) {
            LOG.info("Could not find ScmManager HgBrowser ({}), please upgrade the mercurial plugin", className);
        } catch (Exception e) {
            LOG.warn("failed to create instance of {}", className, e);
        }
        return null;
    }
}
