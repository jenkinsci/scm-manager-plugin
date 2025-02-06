package com.cloudogu.scmmanager.scm.jobdsl;

import com.google.common.base.Strings;
import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.Preconditions;

public class ScmManagerContext implements Context {

    private String serverUrl;
    private String credentialsId;

    public String getServerUrl() {
        return serverUrl;
    }

    public void serverUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void credentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void validate() {
        Preconditions.checkNotNullOrEmpty(serverUrl, "serverUrl is required");
    }
}
