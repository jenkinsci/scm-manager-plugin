package com.cloudogu.scmmanager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.cloudogu.scmmanager.info.JobInformation;
import hudson.model.Run;
import java.net.MalformedURLException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScmV2NotifierProviderTest {

    @Mock
    private AuthenticationFactory authenticationFactory;

    @InjectMocks
    private ScmV2NotifierProvider provider;

    @Mock
    private Run<?, ?> run;

    @Test
    void testGetWithoutMatchingNotifier() throws MalformedURLException {
        JobInformation information = createInformation("sample://one");
        Optional<ScmV2Notifier> notifier = provider.get(run, information);
        assertFalse(notifier.isPresent());
    }

    @Test
    void testGet() throws MalformedURLException {
        applyAuthentication();

        JobInformation information = createInformation("https://scm.scm-manager.org/repo/ns/one");
        ScmV2Notifier notifier = provider.get(run, information).get();

        assertEquals("ns", notifier.getNamespaceAndName().getNamespace());
        assertEquals("one", notifier.getNamespaceAndName().getName());
        assertEquals("https://scm.scm-manager.org", notifier.getInstance().toExternalForm());
        assertSame(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION, notifier.getHttpAuthentication());
    }

    @Test
    void testGetWithFurtherPath() throws MalformedURLException {
        applyAuthentication();

        JobInformation information = createInformation("https://scm.scm-manager.org/repo/ns/one/some/file");
        ScmV2Notifier notifier = provider.get(run, information).get();

        assertEquals("ns", notifier.getNamespaceAndName().getNamespace());
        assertEquals("one", notifier.getNamespaceAndName().getName());
        assertEquals("https://scm.scm-manager.org", notifier.getInstance().toExternalForm());
        assertSame(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION, notifier.getHttpAuthentication());
    }

    @Test
    void testGetWithDotGit() throws MalformedURLException {
        applyAuthentication();

        JobInformation information = createInformation("https://scm.scm-manager.org/repo/ns/one.git");
        ScmV2Notifier notifier = provider.get(run, information).get();

        assertEquals("ns", notifier.getNamespaceAndName().getNamespace());
        assertEquals("one", notifier.getNamespaceAndName().getName());
        assertEquals("https://scm.scm-manager.org", notifier.getInstance().toExternalForm());
        assertSame(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION, notifier.getHttpAuthentication());
    }

    @Test
    void testGetWithContextPath() throws MalformedURLException {
        applyAuthentication();

        JobInformation information = createInformation("https://scm.scm-manager.org/scm/repo/ns/one");
        ScmV2Notifier notifier = provider.get(run, information).get();

        assertEquals("https://scm.scm-manager.org/scm", notifier.getInstance().toExternalForm());
    }

    @Test
    void testGetWithPort() throws MalformedURLException {
        applyAuthentication();

        JobInformation information = createInformation("http://localhost:8080/scm/repo/ns/one");
        ScmV2Notifier notifier = provider.get(run, information).get();

        assertEquals("http://localhost:8080/scm", notifier.getInstance().toExternalForm());
    }

    @Test
    void testGetWithSourceBranch() throws MalformedURLException {
        applyAuthentication();

        JobInformation information = new JobInformation(
                "sample", "https://scm.scm-manager.org/repo/ns/one", "pr-1", "one", true, "simple/branch");
        ScmV2Notifier notifier = provider.get(run, information).get();

        assertEquals("simple/branch", notifier.getSourceBranch());
    }

    private JobInformation createInformation(String s) {
        return new JobInformation("sample", s, "abc", "one", false);
    }

    private void applyAuthentication() {
        when(authenticationFactory.createHttp(run, "one")).thenReturn(AuthenticationFactory.NOOP_HTTP_AUTHENTICATION);
    }
}
