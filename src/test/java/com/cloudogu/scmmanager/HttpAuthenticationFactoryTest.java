package com.cloudogu.scmmanager;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.Descriptor;
import hudson.model.Run;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HttpAuthenticationFactoryTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Mock
    private Run<?, ?> run;

    private final AuthenticationFactory authenticationFactory = new AuthenticationFactory();

    @Test
    public void testCreateWithoutCredentialsId() {
        HttpAuthentication httpAuthentication = authenticationFactory.createHttp(run, null);
        assertSame(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION, httpAuthentication);
    }

    @Test
    public void testCreateWithEmptyCredentialsId() {
        HttpAuthentication httpAuthentication = authenticationFactory.createHttp(run, "");
        assertSame(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION, httpAuthentication);
    }

    @Test
    public void testCreateWithNonExistingCredentials() {
        HttpAuthentication httpAuthentication = authenticationFactory.createHttp(run, "scm-two");
        assertSame(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION, httpAuthentication);
    }

    @Test(expected = CredentialsUnavailableException.class)
    public void testCredentialsUnavailableExceptionIfMissingCredentials() {
        authenticationFactory.createSSH(run, "");
    }

    @Test
    public void testCreateSSHAuthentication() throws IOException, Descriptor.FormException {
        addCredential("scmadmin", "scmadmin", "scmadmin");
        SSHAuthentication authentication = authenticationFactory.createSSH(run, "scmadmin");
        assertSame(authentication.getClass(), SSHAuthentication.class);
    }

    // TODO Replace with lombok
    @Test
    public void testCreate() throws IOException, Descriptor.FormException {
        addCredential("scm-one", "trillian", "secret");

        HttpAuthentication httpAuthentication = authenticationFactory.createHttp(run, "scm-one");
        assertThat(httpAuthentication, CoreMatchers.instanceOf(BasicHttpAuthentication.class));

        BasicHttpAuthentication basic = (BasicHttpAuthentication) httpAuthentication;
        assertEquals("trillian", basic.getUsername());
        assertEquals("secret", basic.getPassword().getPlainText());
    }

    private void addCredential(String id, String username, String password) throws IOException, Descriptor.FormException {
        Credentials c = new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL, id, "description", username, password
        );
        SystemCredentialsProvider instance = SystemCredentialsProvider.getInstance();
        instance.getCredentials().add(c);
        instance.save();
    }

}
