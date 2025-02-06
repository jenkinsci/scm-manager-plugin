package com.cloudogu.scmmanager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cloudogu.scmmanager.info.JobInformation;
import java.util.Optional;
import org.junit.Test;

public class ScmMigratedV1NotifierProviderTest {

    private ScmMigratedV1NotifierProvider provider = new ScmMigratedV1NotifierProvider();

    @Test
    public void testGetWithWrongUrl() {
        Optional<? extends Notifier> notifier = provider.get(null, createInformation("http://localhost/awesome/repo"));
        assertFalse(notifier.isPresent());
    }

    @Test
    public void testGetWithContextPath() {
        assertIsPresent("http://localhost/scm/git/awesome/repo");
    }

    @Test
    public void testGetWithGitUrl() {
        assertIsPresent("https://scm.scm-manager.org/git/awesome/repo");
    }

    @Test
    public void testGetWithHgUrl() {
        assertIsPresent("https://scm.scm-manager.org/hg/awesome/repo");
    }

    @Test
    public void testGetWithSvnUrl() {
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
