package com.cloudogu.scmmanager.scm;

import hudson.plugins.git.GitChangeSet;
import hudson.scm.EditType;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScmManagerGitRepositoryBrowserTest {

  private static final String REPO = "https://scm.hitchhiker.com";

  private ScmManagerGitRepositoryBrowser browser;

  @Before
  public void setUpBrowser() {
    browser = new ScmManagerGitRepositoryBrowser(REPO);
  }

  @Test
  public void shouldReturnChangesetURL() throws IOException {
    GitChangeSet changeSet = mockChangeSet("cde42");

    assertThat(browser.getChangeSetLink(changeSet)).hasPath("/code/changeset/cde42");
  }

  @Test
  public void shouldReturnFileURL() throws IOException {
    GitChangeSet.Path path = mockPath("abc21", "Jenkinsfile", EditType.EDIT);

    assertThat(browser.getFileLink(path)).hasPath("/code/sources/abc21/Jenkinsfile");
  }

  @Test
  public void shouldReturnDequotedFileURL() throws IOException {
    GitChangeSet.Path path = mockPath("sw42", "\"people/Padm\\303\\251 Amidala.json\"", EditType.EDIT);

    assertThat(browser.getFileLink(path)).hasPath("/code/sources/sw42/people/Padmé Amidala.json");
  }

  @Test
  public void shouldReturnDiffLinkForDeletedFiles() throws IOException {
    GitChangeSet.Path path = mockPath("cde42", "README.md", EditType.DELETE);

    assertThat(browser.getFileLink(path)).hasPath("/code/changeset/cde42").hasAnchor("diff-README.md");
  }

  @Test
  public void shouldReturnDiffLink() throws IOException {
    String pathName = "README.md";
    GitChangeSet.Path path = mockPath("abc21", pathName, EditType.EDIT);
    when(path.getSrc()).thenReturn(pathName);
    when(path.getDst()).thenReturn(pathName);
    when(path.getChangeSet().getParentCommit()).thenReturn("cde42");

    assertThat(browser.getDiffLink(path)).hasPath("/code/changeset/abc21").hasAnchor("diff-" + pathName);
  }

  @Test
  public void shouldReturnDequotedDiffLink() throws IOException {
    String pathName = "\"people/Padm\\303\\251 Amidala.json\"";
    GitChangeSet.Path path = mockPath("sw21", pathName, EditType.EDIT);
    when(path.getSrc()).thenReturn(pathName);
    when(path.getDst()).thenReturn(pathName);
    when(path.getChangeSet().getParentCommit()).thenReturn("sw21");

    assertThat(browser.getDiffLink(path)).hasPath("/code/changeset/sw21").hasAnchor("diff-people/Padmé Amidala.json");
  }

  @Test
  public void shouldReturnNullForNonEdits() throws IOException {
    String pathName = "README.md";
    GitChangeSet.Path path = mockPath("abc21", pathName, EditType.DELETE);
    when(path.getSrc()).thenReturn(pathName);
    when(path.getDst()).thenReturn(pathName);
    when(path.getChangeSet().getParentCommit()).thenReturn("cde42");

    assertThat(browser.getDiffLink(path)).isNull();
  }

  @Test
  public void shouldReturnNullWithoutSrc() throws IOException {
    String pathName = "README.md";
    GitChangeSet.Path path = mockPath("abc21", pathName, EditType.EDIT);
    when(path.getDst()).thenReturn(pathName);
    when(path.getChangeSet().getParentCommit()).thenReturn("cde42");

    assertThat(browser.getDiffLink(path)).isNull();
  }

  @Test
  public void shouldReturnNullWithoutDst() throws IOException {
    String pathName = "README.md";
    GitChangeSet.Path path = mockPath("abc21", pathName, EditType.EDIT);
    when(path.getSrc()).thenReturn(pathName);
    when(path.getChangeSet().getParentCommit()).thenReturn("cde42");

    assertThat(browser.getDiffLink(path)).isNull();
  }

  @Test
  public void shouldReturnNullWithoutParentCommit() throws IOException {
    String pathName = "README.md";
    GitChangeSet.Path path = mockPath("abc21", pathName, EditType.EDIT);
    when(path.getSrc()).thenReturn(pathName);
    when(path.getDst()).thenReturn(pathName);

    assertThat(browser.getDiffLink(path)).isNull();
  }

  private GitChangeSet.Path mockPath(String revision, String pathName, EditType editType) {
    GitChangeSet.Path path = mock(GitChangeSet.Path.class);
    GitChangeSet changeSet = mockChangeSet(revision);
    when(path.getChangeSet()).thenReturn(changeSet);
    when(path.getPath()).thenReturn(pathName);
    when(path.getEditType()).thenReturn(editType);
    return path;
  }

  private GitChangeSet mockChangeSet(String revision) {
    GitChangeSet changeSet = mock(GitChangeSet.class);
    when(changeSet.getId()).thenReturn(revision);
    return changeSet;
  }

}
