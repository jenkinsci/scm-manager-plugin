package com.cloudogu.scmmanager.info;

public class JobInformation {

    private final String type;
    private final String url;
    private final String revision;
    private final String credentialsId;
    private final boolean pullRequest;
    private final String sourceBranch;

    public JobInformation(String type, String url, String revision, String credentialsId, boolean pullRequest) {
        this(type, url, revision, credentialsId, pullRequest, null);
    }

    public JobInformation(String type, String url, String revision, String credentialsId, boolean pullRequest, String sourceBranch) {
        this.type = type;
        this.url = url;
        this.revision = revision;
        this.credentialsId = credentialsId;
        this.pullRequest = pullRequest;
        this.sourceBranch = sourceBranch;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getRevision() {
        return revision;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public boolean isPullRequest() {
        return pullRequest;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }
}
