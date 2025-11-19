package com.cloudogu.scmmanager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudogu.scmmanager.info.JobInformation;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ScmMigratedV1NotifierProviderTest {

    private final ScmMigratedV1NotifierProvider provider = new ScmMigratedV1NotifierProvider();

    @Test
    void testGetWithWrongUrl() {
        Optional<? extends Notifier> notifier = provider.get(null, createInformation("http://localhost/awesome/repo"));
        assertFalse(notifier.isPresent());
    }

    @Test
    void testGetWithContextPath() {
        assertIsPresent("http://localhost/scm/git/awesome/repo");
    }

    @Test
    void testGetWithGitUrl() {
        assertIsPresent("https://scm.scm-manager.org/git/awesome/repo");
    }

    @Test
    void testGetWithHgUrl() {
        assertIsPresent("https://scm.scm-manager.org/hg/awesome/repo");
    }

    @Test
    void testGetWithSvnUrl() {
        assertIsPresent("https://scm.scm-manager.org/svn/awesome/repo");
    }

    private void assertIsPresent(String s) {
        JobInformation information = createInformation(s);
        Optional<ScmMigratedV1Notifier> notifier = provider.get(null, information);
        assertTrue(notifier.isPresent());
    }

    private JobInformation createInformation(String url) {
        return new JobInformation("migrated", url, "abc", "v1", false);
    }
}
