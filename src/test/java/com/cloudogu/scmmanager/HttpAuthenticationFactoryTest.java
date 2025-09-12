package com.cloudogu.scmmanager;

import static org.junit.jupiter.api.Assertions.*;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.Descriptor;
import hudson.model.Run;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@WithJenkins
class HttpAuthenticationFactoryTest {

    private final AuthenticationFactory authenticationFactory = new AuthenticationFactory();

    @Mock
    private Run<?, ?> run;

    private JenkinsRule j;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        j = rule;
    }

    @Test
    void testCreateWithoutCredentialsId() {
        HttpAuthentication httpAuthentication = authenticationFactory.createHttp(run, null);
        assertSame(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION, httpAuthentication);
    }

    @Test
    void testCreateWithEmptyCredentialsId() {
        HttpAuthentication httpAuthentication = authenticationFactory.createHttp(run, "");
        assertSame(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION, httpAuthentication);
    }

    @Test
    void testCreateWithNonExistingCredentials() {
        HttpAuthentication httpAuthentication = authenticationFactory.createHttp(run, "scm-two");
        assertSame(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION, httpAuthentication);
    }

    @Test
    void testCredentialsUnavailableExceptionIfMissingCredentials() {
        assertThrows(CredentialsUnavailableException.class, () -> authenticationFactory.createSSH(run, ""));
    }

    @Test
    void testCreateSSHAuthentication() throws IOException, Descriptor.FormException {
        addCredential("scmadmin", "scmadmin", "scmadmin");
        SSHAuthentication authentication = authenticationFactory.createSSH(run, "scmadmin");
        assertSame(SSHAuthentication.class, authentication.getClass());
    }

    // TODO Replace with lombok
    @Test
    void testCreate() throws IOException, Descriptor.FormException {
        addCredential("scm-one", "trillian", "secret");

        HttpAuthentication httpAuthentication = authenticationFactory.createHttp(run, "scm-one");
        assertInstanceOf(BasicHttpAuthentication.class, httpAuthentication);

        BasicHttpAuthentication basic = (BasicHttpAuthentication) httpAuthentication;
        assertEquals("trillian", basic.getUsername());
        assertEquals("secret", basic.getPassword().getPlainText());
    }

    private void addCredential(String id, String username, String password)
            throws IOException, Descriptor.FormException {
        Credentials c =
                new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, id, "description", username, password);
        SystemCredentialsProvider instance = SystemCredentialsProvider.getInstance();
        instance.getCredentials().add(c);
        instance.save();
    }
}
