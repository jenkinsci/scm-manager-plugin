package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.InvisibleAction;

public class ScmManagerApiData extends InvisibleAction {

    @NonNull
    private final String serverUrl;
    @NonNull
    private final String credentialsId;
    @NonNull
    private final String namespace;
    @NonNull
    private final String name;

    public ScmManagerApiData(@NonNull String serverUrl,
                             @NonNull String credentialsId,
                             @NonNull String namespace,
                             @NonNull String name) {
        this.serverUrl = serverUrl;
        this.credentialsId = credentialsId;
        this.namespace = namespace;
        this.name = name;
    }

    @NonNull
    public String getServerUrl() {
        return serverUrl;
    }

    @NonNull
    public String getCredentialsId() {
        return credentialsId;
    }

    @NonNull
    public String getNamespace() {
        return namespace;
    }

    @NonNull
    public String getName() {
        return name;
    }
}
