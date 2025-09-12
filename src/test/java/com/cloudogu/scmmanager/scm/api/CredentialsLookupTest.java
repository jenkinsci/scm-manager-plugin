package com.cloudogu.scmmanager.scm.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.Descriptor;
import java.io.IOException;
import jenkins.scm.api.SCMSourceOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@WithJenkins
class CredentialsLookupTest {

    private final CredentialsLookup credentialsLookup = new CredentialsLookup();

    @Mock
    private SCMSourceOwner owner;

    private JenkinsRule j;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        j = rule;
    }

    @Test
    void shouldReturnHttpCredentials() throws IOException, Descriptor.FormException {
        addUsernamePasswordCredentials("tricia", "trillian", "secret");
        StandardUsernamePasswordCredentials credentials =
                credentialsLookup.http("http://hog", "tricia").lookup(owner);
        assertThat(credentials).isNotNull();
    }

    @Test
    void shouldReturnCredentialsFromItemGroup() throws IOException, Descriptor.FormException {
        addUsernamePasswordCredentials("tricia", "trillian", "secret");
        StandardUsernamePasswordCredentials credentials =
                credentialsLookup.http("http://hog", "tricia").lookup(j.getInstance());
        assertThat(credentials).isNotNull();
    }

    @Test
    void shouldReturnSshUsernamePasswordCredentials() throws IOException, Descriptor.FormException {
        addUsernamePasswordCredentials("dent", "adent", "secret123");
        StandardUsernameCredentials credentials =
                credentialsLookup.ssh("ssh://hog", "dent").lookup(owner);
        assertThat(credentials).isNotNull();
    }

    @Test
    void shouldReturnSshPrivateKeyCredentials() throws IOException {
        addPrivateKeyCredentials("slarti", "slarti", "private-ssh-key", "");
        StandardUsernameCredentials credentials =
                credentialsLookup.ssh("ssh://hog", "slarti").lookup(owner);
        assertThat(credentials).isNotNull();
    }

    @Test
    void shouldThrowExceptionForNonExistingCredentials() {
        assertThrows(
                CredentialsUnavailableException.class,
                () -> credentialsLookup.ssh("ssh://hog", "slarti").lookup(owner));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForNonSshUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> credentialsLookup.ssh("http://hog", "slarti").lookup(owner));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForNonHttpUrl() {
        assertThrows(
                IllegalArgumentException.class,
                () -> credentialsLookup.ssh("http://hog", "slarti").lookup(owner));
    }

    private void addPrivateKeyCredentials(String id, String username, String key, String passphrase)
            throws IOException {
        BasicSSHUserPrivateKey.PrivateKeySource source = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(key);
        BasicSSHUserPrivateKey sshUserPrivateKey = new BasicSSHUserPrivateKey(
                CredentialsScope.GLOBAL, id, username, source, passphrase, username + "-desc");
        addCredential(sshUserPrivateKey);
    }

    private void addUsernamePasswordCredentials(String id, String username, String password)
            throws IOException, Descriptor.FormException {
        UsernamePasswordCredentials upc = new UsernamePasswordCredentialsImpl(
                CredentialsScope.GLOBAL, id, username + "-desc", username, password);
        addCredential(upc);
    }

    private void addCredential(com.cloudbees.plugins.credentials.Credentials credentials) throws IOException {
        SystemCredentialsProvider instance = SystemCredentialsProvider.getInstance();
        instance.getCredentials().add(credentials);
        instance.save();
    }
}
