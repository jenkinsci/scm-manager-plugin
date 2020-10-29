package com.cloudogu.scmmanager.scm.api;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import jenkins.scm.api.SCMSourceOwner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmManagerApiFactoryTest {

  @Rule
  public JenkinsRule jenkinsRule = new JenkinsRule();

  @Mock
  private SCMSourceOwner owner;

  @Mock
  private StandardUsernamePasswordCredentials credentials;

  @Mock
  private CredentialsLookup credentialsLookup;

  @InjectMocks
  private ScmManagerApiFactory apiFactory;

  @Test
  public void shouldCreateAnonymousHttpApi() {
    ScmManagerApi api = apiFactory.anonymous("https://scm.hitchhiker.com");
    assertThat(api.getProtocol()).isEqualTo("http");
  }

  @Test
  public void shouldCreateHttpApi() {
    when(credentialsLookup.http(owner, "https://scm.hitchhiker.com", "scm-creds")).thenReturn(credentials);
    ScmManagerApi api = apiFactory.create(owner, "https://scm.hitchhiker.com", "scm-creds");
    assertThat(api.getProtocol()).isEqualTo("http");
  }

  @Test
  public void shouldCreateSshApi() {
    when(credentialsLookup.http(owner, "ssh://scm.hitchhiker.com", "scm-creds")).thenReturn(credentials);
    ScmManagerApi api = apiFactory.create(owner, "ssh://scm.hitchhiker.com", "scm-creds");
    assertThat(api.getProtocol()).isEqualTo("ssh");
  }

}
