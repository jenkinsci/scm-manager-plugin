package com.cloudogu.scmmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import com.cloudogu.scmmanager.info.JobInformation;
import hudson.model.Run;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScmV2SshNotifierProviderTest {

    @InjectMocks
    private ScmV2SshNotifierProvider provider;

    @Mock
    private AuthenticationFactory authenticationFactory;

    @Mock
    private Run<?, ?> run;

    @Test
    public void testGetWithoutMatchingNotifier() {
        JobInformation information = createInformation("sample://one");
        Optional<ScmV2SshNotifier> notifier = provider.get(run, information);
        assertFalse(notifier.isPresent());
    }

    @Test
    public void testCreateNotifier() {
        applyAuthentication(new SSHAuthentication(null));
        JobInformation information = createInformation("ssh://scm@scm-manager.org:8889/repo/ns/one");
        ScmV2SshNotifier notifier = provider.get(run, information).get();

        NamespaceAndName repository = notifier.getConnection().mustGetRepository();
        assertEquals("ns", repository.getNamespace());
        assertEquals("one", repository.getName());
        assertEquals(notifier.getConnection().getConnection().getHostname(), "scm-manager.org");
        assertEquals(notifier.getConnection().getConnection().getPort(), 8889);
    }

    private JobInformation createInformation(String s) {
        return new JobInformation("sample", s, "abc", "one", false);
    }

    private void applyAuthentication(SSHAuthentication sshAuthentication) {
        when(authenticationFactory.createSSH(run, "one")).thenReturn(sshAuthentication);
    }
}
