package com.cloudogu.scmmanager.scm;

import static com.cloudogu.scmmanager.scm.ScmTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import jenkins.scm.api.SCMRevision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ScmManagerSourceRetrieverTest {

    @Mock
    private ScmManagerApi api;

    private ScmManagerSourceRetriever handler;

    @BeforeEach
    void beforeEach() {
        when(api.getRepository(NAMESPACE, NAME))
                .thenReturn(CompletableFuture.completedFuture(new Repository(NAMESPACE, NAME, "git")));

        handler = ScmManagerSourceRetriever.create(api, NAMESPACE, NAME, Collections.emptyList());
    }

    @Test
    void shouldCreateProbeForBranch() throws Exception {
        ScmManagerHead head = branch("main");
        ScmManagerRevision revision = revision(head, "abc42");
        ScmManagerApiProbe probe = handler.probe(head, revision);
        assertThat(probe.revision().get()).isEqualTo("abc42");
    }

    @Test
    void shouldCreateProbeForPullRequest() throws Exception {
        ScmManagerPullRequestHead head = pullRequest("PR-42", branch("main"), branch("develop"));
        ScmManagerPullRequestRevision revision = pullRequestRevision(head, "abc21", "cde42");
        ScmManagerApiProbe probe = handler.probe(head, revision);
        assertThat(probe.revision().get()).isEqualTo("cde42");
    }

    @Test
    void shouldThrowExceptionForUnknownRevision() {
        ScmManagerHead head = branch("main");

        assertThrows(
                IllegalArgumentException.class,
                () -> handler.probe(head, new SCMRevision(head) {

                    @Override
                    public boolean equals(Object obj) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }
                }));
    }
}
