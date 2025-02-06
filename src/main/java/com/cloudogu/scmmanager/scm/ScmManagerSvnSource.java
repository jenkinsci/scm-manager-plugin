package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.scm.SubversionSCM;
import hudson.scm.subversion.UpdateUpdater;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.impl.UncategorizedSCMHeadCategory;
import jenkins.scm.impl.subversion.SubversionSCMSource;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

public class ScmManagerSvnSource extends SubversionSCMSource {

    private final String serverUrl;
    private final String repository;

    @DataBoundConstructor
    public ScmManagerSvnSource(String id, String serverUrl, String repository, String credentialsId) {
        super(id, createRepoLink(serverUrl, repository));
        this.setCredentialsId(credentialsId);
        this.serverUrl = serverUrl;
        this.repository = repository;
    }

    private static String createRepoLink(String serverUrl, String repository) {
        String[] parts = repository.split("/");
        return new LinkBuilder(serverUrl, parts[0], parts[1]).repo();
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getRepository() {
        return repository;
    }

    @NonNull
    @Override
    public SubversionSCM build(@NonNull SCMHead head, SCMRevision revision) {
        // mostly a copy from SubversionSCMSource
        // we have to rewrite the method to set the repository browser

        String repoLink = createRepoLink(serverUrl, repository);

        if (revision != null && !head.equals(revision.getHead())) {
            revision = null;
        }
        // dow we need to support ScmManagerRevision
        if (revision != null && !(revision instanceof SCMRevisionImpl)) {
            revision = null;
        }
        StringBuilder remote = new StringBuilder(repoLink);
        if (!repoLink.endsWith("/")) {
            remote.append('/');
        }
        remote.append(head.getName());
        if (revision != null) {
            remote.append('@').append(((SCMRevisionImpl) revision).getRevision());
        } else if (remote.indexOf("@") != -1) {
            // name contains an @ so need to ensure there is an @ at the end of the name
            remote.append('@');
        }

        List<SubversionSCM.ModuleLocation> locations = SubversionSCM.ModuleLocation.parse(
            new String[]{remote.toString()}, new String[]{getCredentialsId()}, new String[]{"."},
            null, null, null
        );

        return new SubversionSCM(
            locations, new UpdateUpdater(), new ScmManagerSvnRepositoryBrowser(repoLink),
            null, null, null, null,
            null, false, false, null,
            false
        );
    }

    @Extension(optional = true)
    @Symbol("scmManagerSvn")
    public static class DescriptorImpl extends ScmManagerSourceDescriptor {

        public static final String DEFAULT_INCLUDES = Subversion.DEFAULT_INCLUDES;
        public static final String DEFAULT_EXCLUDES = Subversion.DEFAULT_EXCLUDES;

        public DescriptorImpl() {
            super(new ScmManagerApiFactory(), r -> "svn".equals(r.getType()));
        }

        @Override
        protected String createRepositoryOption(Repository repository) {
            return repository.getNamespace() + "/" + repository.getName();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "SCM-Manager (svn)";
        }

        @NonNull
        @Override
        protected SCMHeadCategory[] createCategories() {
            return new SCMHeadCategory[]{
                UncategorizedSCMHeadCategory.DEFAULT
            };
        }
    }
}
