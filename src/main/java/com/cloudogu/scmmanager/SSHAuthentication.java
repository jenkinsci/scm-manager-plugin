package com.cloudogu.scmmanager;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHAuthenticator;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.trilead.ssh2.Connection;
import hudson.util.LogTaskListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SSHAuthentication {

    private static final Logger LOG = Logger.getLogger(SSHAuthentication.class.getName());

    private final StandardUsernameCredentials credentials;

    SSHAuthentication(StandardUsernameCredentials credentials) {
        this.credentials = credentials;
    }

    public static SSHAuthentication from(StandardUsernameCredentials credentials) {
        return new SSHAuthentication(credentials);
    }

    void authenticate(Connection connection) throws IOException {
        try {
            SSHAuthenticator<Connection, StandardUsernameCredentials> authenticator = SSHAuthenticator.newInstance(
                connection, credentials, credentials.getUsername()
            );
            if (!authenticator.authenticate(new LogTaskListener(LOG, Level.INFO))) {
                throw new SshConnectionFailedException("ssh authentication failed");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SshConnectionFailedException("failed to authenticate", e);
        }

    }
}
