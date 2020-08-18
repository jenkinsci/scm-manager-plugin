package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.Changeset;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerFile;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMProbe;
import jenkins.scm.api.SCMProbeStat;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class ScmManagerApiProbe extends SCMProbe {

  private static final long serialVersionUID = -1L;

  private final transient ScmManagerApi api;
  private final Repository repository;
  private final SCMHead head;
  private final ScmManagerRevision revision;

  private Date lastModified;

  public ScmManagerApiProbe(@NonNull ScmManagerApi api, @NonNull Repository repository, @NonNull SCMHead head, @NonNull ScmManagerRevision revision) {
    this.api = api;
    this.repository = repository;
    this.head = head;
    this.revision = revision;
  }

  @VisibleForTesting
  ScmManagerRevision getRevision() {
    return revision;
  }

  @Override
  public String name() {
    return head.getName();
  }

  @Override
  public long lastModified() {
    if (lastModified != null) {
      return lastModified.getTime();
    }
    CompletableFuture<Changeset> future = api.getChangeset(repository, revision.getRevision());
    Changeset changeset = ScmManagerApi.fetchUnchecked(future);
    lastModified = changeset.getDate();
    return lastModified.getTime();
  }

  @NonNull
  @Override
  public SCMProbeStat stat(@NonNull String path) throws IOException {
    CompletableFuture<ScmManagerFile> future = api.getFileObject(repository, revision.getRevision(), path);
    ScmManagerFile file = ScmManagerApi.fetchChecked(future);
    return SCMProbeStat.fromType(file.getType());
  }

  @Override
  public void close() {
    // we have nothing to close
  }
}
