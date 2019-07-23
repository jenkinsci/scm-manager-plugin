package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.ScmInformation;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    ScmInformation information = createInformation(s);
    Optional<ScmMigratedV1Notifier> notifier = provider.get(null, information);
    assertTrue(notifier.isPresent());
  }

  private ScmInformation createInformation(String url) {
    return new ScmInformation("migrated", url, "abc", "v1");
  }

}
