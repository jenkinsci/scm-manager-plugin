package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.Changeset;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerFile;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMProbeStat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static com.cloudogu.scmmanager.scm.ScmTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmManagerApiProbeTest {

  @Mock
  private ScmManagerApi api;

  @Test
  public void shouldUseNameFromHead() {
    ScmManagerApiProbe probe = probe();
    assertThat(probe.name()).isEqualTo("main");
  }

  @Test
  public void shouldFetchChangeset() {
    Date date = new Date();
    mockApiChangeset("abc42", date);
    assertThat(probe().lastModified()).isEqualTo(date.getTime());
  }

  @Test
  public void shouldFetchChangesetOnlyOnce() {
    Date date = new Date();
    mockApiChangeset("cde21", date);

    ScmManagerApiProbe probe = probe("develop", "cde21");
    assertThat(probe.lastModified()).isEqualTo(date.getTime());
    assertThat(probe.lastModified()).isEqualTo(date.getTime());

    // should be called only once
    verify(api).getChangeset(REPOSITORY, "cde21");
  }

  @Test
  public void shouldFetchFile() throws IOException {
    mockApiFileStat("cde21", "Jenkinsfile", SCMFile.Type.REGULAR_FILE);
    ScmManagerApiProbe probe = probe("develop", "cde21");
    assertThat(probe.stat("Jenkinsfile").exists()).isTrue();
  }

  private void mockApiFileStat(String revision, String path, SCMFile.Type type) {
    when(api.getFileObject(REPOSITORY, revision, path)).thenReturn(futureFile(path, type));
  }

  private CompletableFuture<ScmManagerFile> futureFile(String path, SCMFile.Type type) {
    return CompletableFuture.completedFuture(
      new ScmManagerFile(path, type)
    );
  }

  private void mockApiChangeset(String revision, Date date) {
    when(api.getChangeset(REPOSITORY, revision)).thenReturn(futureChangeset(revision, date));
  }

  private ScmManagerApiProbe probe() {
    return probe("main", "abc42");
  }

  private CompletableFuture<Changeset> futureChangeset(String id, Date date) {
    return CompletableFuture.completedFuture(new Changeset(id, date));
  }

  private ScmManagerApiProbe probe(String branchName, String revisionString) {
    ScmManagerHead branch = branch(branchName);
    return new ScmManagerApiProbe(api, REPOSITORY, branch, revision(branch, revisionString));
  }

}
