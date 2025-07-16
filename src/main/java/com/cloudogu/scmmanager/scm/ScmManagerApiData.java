package com.cloudogu.scmmanager.scm;

import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.InvisibleAction;
import java.util.Objects;

public class ScmManagerApiData extends InvisibleAction {

    @NonNull
    private final String serverUrl;

    @NonNull
    private final String credentialsId;

    @NonNull
    private final String namespace;

    @NonNull
    private final String name;

    public ScmManagerApiData(
            @NonNull String serverUrl, @NonNull String credentialsId, @NonNull String namespace, @NonNull String name) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScmManagerApiData that = (ScmManagerApiData) o;
        return serverUrl.equals(that.serverUrl)
                && credentialsId.equals(that.credentialsId)
                && namespace.equals(that.namespace)
                && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverUrl, credentialsId, namespace, name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("serverUrl", serverUrl)
                .add("credentialsId", credentialsId)
                .add("namespace", namespace)
                .add("name", name)
                .toString();
    }
}
