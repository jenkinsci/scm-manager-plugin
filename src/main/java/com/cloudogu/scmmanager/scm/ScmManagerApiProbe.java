package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.Changeset;
import com.cloudogu.scmmanager.scm.api.Futures;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerFile;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMProbe;
import jenkins.scm.api.SCMProbeStat;

public class ScmManagerApiProbe extends SCMProbe {

    private static final long serialVersionUID = -1L;

    private final transient ScmManagerApi api;
    private final Repository repository;
    private final SCMHead head;

    private transient CompletableFuture<ScmManagerRevision> revision;

    private Date lastModified;

    public ScmManagerApiProbe(
            @NonNull ScmManagerApi api,
            @NonNull Repository repository,
            @NonNull SCMHead head,
            @CheckForNull ScmManagerRevision revision) {
        this.api = api;
        this.repository = repository;
        this.head = head;
        if (revision != null) {
            this.revision = CompletableFuture.completedFuture(revision);
        }
    }

    @VisibleForTesting
    CompletableFuture<String> revision() {
        if (revision == null) {
            revision = new HeadResolver(api, repository).resolve(head);
        }
        return revision.thenApply(ScmManagerRevision::getRevision);
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
        CompletableFuture<Changeset> future = revision().thenCompose(r -> api.getChangeset(repository, r));
        Changeset changeset = Futures.resolveUnchecked(future);
        lastModified = changeset.getDate();
        return lastModified.getTime();
    }

    @NonNull
    @Override
    public SCMProbeStat stat(@NonNull String path) throws IOException {
        CompletableFuture<ScmManagerFile> future = revision().thenCompose(r -> api.getFileObject(repository, r, path));
        ScmManagerFile file = Futures.resolveChecked(future);
        return SCMProbeStat.fromType(file.getType());
    }

    @Override
    public void close() {
        // we have nothing to close
    }
}
