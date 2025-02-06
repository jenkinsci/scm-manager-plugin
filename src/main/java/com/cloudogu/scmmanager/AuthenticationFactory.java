package com.cloudogu.scmmanager;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import hudson.model.Run;
import java.util.Collections;

class AuthenticationFactory {

    @VisibleForTesting
    static final HttpAuthentication NOOP_HTTP_AUTHENTICATION = requestBuilder -> {};

    HttpAuthentication createHttp(Run<?, ?> run, String credentialsId) {
        if (Strings.isNullOrEmpty(credentialsId)) {
            return NOOP_HTTP_AUTHENTICATION;
        }
        StandardUsernamePasswordCredentials credentials = CredentialsProvider.findCredentialById(
                credentialsId, StandardUsernamePasswordCredentials.class, run, Collections.emptyList());
        if (credentials == null) {
            return NOOP_HTTP_AUTHENTICATION;
        }

        return new BasicHttpAuthentication(credentials.getUsername(), credentials.getPassword());
    }

    SSHAuthentication createSSH(Run<?, ?> run, String credentialsId) {
        if (Strings.isNullOrEmpty(credentialsId)) {
            throw new CredentialsUnavailableException("could not found credentials for ssh authentication");
        }

        StandardUsernameCredentials credentials = CredentialsProvider.findCredentialById(
                credentialsId, StandardUsernameCredentials.class, run, Collections.emptyList());
        if (credentials == null) {
            throw new CredentialsUnavailableException(
                    String.format("could not find credentials by id: %s", credentialsId));
        }
        return new SSHAuthentication(credentials);
    }
}
