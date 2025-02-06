package com.cloudogu.scmmanager.scm;

import hudson.scm.EditType;
import hudson.scm.SubversionChangeLogSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScmManagerSvnRepositoryBrowserTest {

    private static final String REPO = "https://scm.hitchhiker.com";

    private ScmManagerSvnRepositoryBrowser browser;

    @Before
    public void setUpBrowser() {
        browser = new ScmManagerSvnRepositoryBrowser(REPO);
    }

    @Test
    public void shouldReturnDiffURL() throws IOException {
        SubversionChangeLogSet.Path path = mockPath(42, "Jenkinsfile", EditType.EDIT);
        assertThat(browser.getDiffLink(path)).hasPath("/code/changeset/42").hasAnchor("diff-Jenkinsfile");
    }

    @Test
    public void shouldReturnDiffURLEvenForNonEdits() throws IOException {
        SubversionChangeLogSet.Path path = mockPath(21, "README.md", EditType.ADD);
        assertThat(browser.getDiffLink(path)).hasPath("/code/changeset/21").hasAnchor("diff-README.md");
    }

    @Test
    public void shouldReturnDiffAnchorWithoutLeadingSlash() throws IOException {
        SubversionChangeLogSet.Path path = mockPath(42, "/trunk/Jenkinsfile", EditType.EDIT);
        assertThat(browser.getDiffLink(path)).hasPath("/code/changeset/42").hasAnchor("diff-trunk/Jenkinsfile");
    }

    @Test
    public void shouldReturnSourceURL() throws IOException {
        SubversionChangeLogSet.Path path = mockPath(42, "/trunk/README.md", EditType.ADD);
        assertThat(browser.getFileLink(path)).hasPath("/code/sources/42/trunk/README.md");
    }

    @Test
    public void shouldReturnDiffURLForDeletion() throws IOException {
        SubversionChangeLogSet.Path path = mockPath(42, "/trunk/README.md", EditType.DELETE);
        assertThat(browser.getFileLink(path)).hasPath("/code/changeset/42").hasAnchor("diff-trunk/README.md");
    }

    @Test
    public void shouldReturnChangesetURL() throws IOException {
        SubversionChangeLogSet.LogEntry entry = mockLogEntry(42);
        assertThat(browser.getChangeSetLink(entry)).hasPath("/code/changeset/42");
    }

    private SubversionChangeLogSet.LogEntry mockLogEntry(int revision) {
        SubversionChangeLogSet.LogEntry entry = mock(SubversionChangeLogSet.LogEntry.class);
        when(entry.getRevision()).thenReturn(revision);
        return entry;
    }

    private SubversionChangeLogSet.Path mockPath(int revision, String path, EditType type) {
        SubversionChangeLogSet.Path svnPath = mock(SubversionChangeLogSet.Path.class, Answers.RETURNS_DEEP_STUBS);
        when(svnPath.getEditType()).thenReturn(type);
        when(svnPath.getLogEntry().getRevision()).thenReturn(revision);
        when(svnPath.getValue()).thenReturn(path);
        return svnPath;
    }


}
