package com.cloudogu.scmmanager.scm.api;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import jenkins.scm.api.SCMSourceOwner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class CredentialsLookupTest {

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();

  @Mock
  private SCMSourceOwner owner;

  private final CredentialsLookup credentialsLookup = new CredentialsLookup();

  @Test
  public void shouldReturnHttpCredentials() throws IOException {
    addUsernamePasswordCredentials("tricia", "trillian", "secret");
    StandardUsernamePasswordCredentials credentials = credentialsLookup.http("http://hog", "tricia").lookup(owner);
    assertThat(credentials).isNotNull();
  }

  @Test
  public void shouldReturnCredentialsFromItemGroup() throws IOException {
    addUsernamePasswordCredentials("tricia", "trillian", "secret");
    StandardUsernamePasswordCredentials credentials = credentialsLookup.http("http://hog", "tricia").lookup(jenkins.getInstance());
    assertThat(credentials).isNotNull();
  }

  @Test
  public void shouldReturnSshUsernamePasswordCredentials() throws IOException {
    addUsernamePasswordCredentials("dent", "adent", "secret123");
    StandardUsernameCredentials credentials = credentialsLookup.ssh("ssh://hog", "dent").lookup(owner);
    assertThat(credentials).isNotNull();
  }

  @Test
  public void shouldReturnSshPrivateKeyCredentials() throws IOException {
    addPrivateKeyCredentials("slarti", "slarti", "private-ssh-key", "");
    StandardUsernameCredentials credentials = credentialsLookup.ssh("ssh://hog", "slarti").lookup(owner);
    assertThat(credentials).isNotNull();
  }

  @Test(expected = CredentialsUnavailableException.class)
  public void shouldThrowExceptionForNonExistingCredentials() {
    credentialsLookup.ssh("ssh://hog", "slarti").lookup(owner);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionForNonSshUrl() {
    credentialsLookup.ssh( "http://hog", "slarti").lookup(owner);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionForNonHttpUrl() {
    credentialsLookup.ssh("http://hog", "slarti").lookup(owner);
  }

  private void addPrivateKeyCredentials(String id, String username, String key, String passphrase) throws IOException {
    BasicSSHUserPrivateKey.PrivateKeySource source = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(key);
    BasicSSHUserPrivateKey sshUserPrivateKey = new BasicSSHUserPrivateKey(
      CredentialsScope.GLOBAL, id, username, source, passphrase, username + "-desc"
    );
    addCredential(sshUserPrivateKey);
  }

  private void addUsernamePasswordCredentials(String id, String username, String password) throws IOException {
    UsernamePasswordCredentials upc = new UsernamePasswordCredentialsImpl(
      CredentialsScope.GLOBAL, id, username + "-desc", username, password
    );
    addCredential(upc);
  }

  private void addCredential(com.cloudbees.plugins.credentials.Credentials credentials) throws IOException {
    SystemCredentialsProvider instance = SystemCredentialsProvider.getInstance();
    instance.getCredentials().add(credentials);
    instance.save();
  }

}
