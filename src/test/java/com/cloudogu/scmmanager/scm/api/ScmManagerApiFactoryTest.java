package com.cloudogu.scmmanager.scm.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import jenkins.scm.api.SCMSourceOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@WithJenkins
class ScmManagerApiFactoryTest {

    @Mock
    private SCMSourceOwner owner;

    @Mock
    private StandardUsernamePasswordCredentials credentials;

    @Mock
    private CredentialsLookup credentialsLookup;

    @InjectMocks
    private ScmManagerApiFactory apiFactory;

    private JenkinsRule j;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        j = rule;
    }

    @Test
    void shouldCreateAnonymousHttpApi() {
        ScmManagerApi api = apiFactory.anonymous("https://scm.hitchhiker.com");
        assertThat(api.getProtocol()).isEqualTo("http");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateHttpApi() {
        CredentialsLookup.Lookup<StandardUsernamePasswordCredentials> lookup = mock(CredentialsLookup.Lookup.class);
        when(lookup.lookup(owner)).thenReturn(credentials);
        when(credentialsLookup.http("https://scm.hitchhiker.com", "scm-creds")).thenReturn(lookup);

        ScmManagerApi api = apiFactory.create(owner, "https://scm.hitchhiker.com", "scm-creds");
        assertThat(api.getProtocol()).isEqualTo("http");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateHttpApiFromItemGroup() {
        CredentialsLookup.Lookup<StandardUsernamePasswordCredentials> lookup = mock(CredentialsLookup.Lookup.class);
        when(lookup.lookup(j.getInstance())).thenReturn(credentials);
        when(credentialsLookup.http("https://scm.hitchhiker.com", "scm-creds")).thenReturn(lookup);

        ScmManagerApi api = apiFactory.create(j.getInstance(), "https://scm.hitchhiker.com", "scm-creds");
        assertThat(api.getProtocol()).isEqualTo("http");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateSshApi() {
        CredentialsLookup.Lookup<StandardUsernameCredentials> lookup = mock(CredentialsLookup.Lookup.class);
        when(lookup.lookup(owner)).thenReturn(credentials);
        when(credentialsLookup.ssh("ssh://scm.hitchhiker.com", "scm-creds")).thenReturn(lookup);

        ScmManagerApi api = apiFactory.create(owner, "ssh://scm.hitchhiker.com", "scm-creds");
        assertThat(api.getProtocol()).isEqualTo("ssh");
    }
}
