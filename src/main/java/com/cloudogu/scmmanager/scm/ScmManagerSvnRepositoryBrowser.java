package com.cloudogu.scmmanager.scm;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SubversionChangeLogSet;
import hudson.scm.SubversionRepositoryBrowser;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ScmManagerSvnRepositoryBrowser extends SubversionRepositoryBrowser {

    private final LinkBuilder builder;

    @DataBoundConstructor
    public ScmManagerSvnRepositoryBrowser(String repoUrl) {
        this.builder = new LinkBuilder(repoUrl);
    }

    public String getRepoUrl() {
        return builder.repo();
    }

    @Override
    public URL getDiffLink(SubversionChangeLogSet.Path path) throws IOException {
        return diff(path);
    }

    private String removeLeadingSlash(String value) {
        if (value.startsWith("/")) {
            return value.substring(1);
        }
        return value;
    }

    private String getRevision(SubversionChangeLogSet.Path path) {
        return getRevision(path.getLogEntry());
    }

    private String getRevision(SubversionChangeLogSet.LogEntry changeset) {
        return String.valueOf(changeset.getRevision());
    }

    @Override
    public URL getFileLink(SubversionChangeLogSet.Path path) throws IOException {
        if (path.getEditType() == EditType.DELETE) {
            return diff(path);
        }
        return new URL(builder.source(getRevision(path), path.getValue()));
    }

    private URL diff(SubversionChangeLogSet.Path path) throws MalformedURLException {
        return new URL(builder.diff(getRevision(path), removeLeadingSlash(path.getValue())));
    }

    @Override
    public URL getChangeSetLink(SubversionChangeLogSet.LogEntry changeSet) throws IOException {
        return new URL(builder.changeset(getRevision(changeSet)));
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
        @Override
        public String getDisplayName() {
            return "SCM-Manager";
        }
    }
}
