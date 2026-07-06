package com.cloudogu.scmmanager.scm.env;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.cloudogu.scmmanager.scm.ScmManagerApiData;
import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.PullRequest;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import hudson.EnvVars;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import java.util.List;
import jenkins.branch.Branch;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHead;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PullRequestLabelsEnvContributorTest {

    private static final String SERVER_URL = "http://localhost:8080";
    private static final String CREDENTIALS_ID = "CREDS_ID";
    private static final String NAMESPACE = "NAMESPACE";
    private static final String NAME = "NAME";
    private static final CloneInformation CLONE_INFORMATION = new CloneInformation("git", "http://example.com/repo");

    @Mock
    private ScmManagerApiFactory apiFactory;

    @Mock
    private ScmManagerApi api;

    @Mock
    private Jenkins owner;

    @Mock
    private WorkflowJob job;

    @Mock
    private WorkflowRun run;

    @Mock
    private EnvVars envVars;

    @Mock
    private TaskListener listener;

    @Mock
    private BranchJobProperty branchJobProperty;

    private PullRequestLabelsEnvContributor envContributor;

    @BeforeEach
    void beforeEach() {
        lenient().when(job.getParent()).thenReturn(owner);
        lenient().when(run.getParent()).thenReturn(job);
        envContributor = new PullRequestLabelsEnvContributor(apiFactory);
    }

    @Test
    void shouldInjectCurrentPullRequestLabelsFromApi() {
        useBranch(pullRequestHead(List.of("old-label")));
        Repository repository = setupApiCall(pullRequestWithLabels("backend", "needs review", "comma,label"));

        envContributor.buildEnvironmentFor(run, envVars, listener);

        verify(api).getPullRequest(repository, "42");
        verify(envVars).put("SCMM_PR_LABELS", "[\"backend\",\"needs review\",\"comma,label\"]");
        verify(envVars).put("SCMM_PR_LABEL_COUNT", "3");
        verify(envVars).put("SCMM_PR_LABEL_1", "backend");
        verify(envVars).put("SCMM_PR_LABEL_2", "needs review");
        verify(envVars).put("SCMM_PR_LABEL_3", "comma,label");
        verifyNoMoreInteractions(envVars);
    }

    @Test
    void shouldFallBackToPullRequestHeadLabelsWhenApiDataIsMissing() {
        useBranch(pullRequestHead(List.of("backend", "needs review", "comma,label")));

        envContributor.buildEnvironmentFor(run, envVars, listener);

        verifyNoInteractions(apiFactory);
        verify(envVars).put("SCMM_PR_LABELS", "[\"backend\",\"needs review\",\"comma,label\"]");
        verify(envVars).put("SCMM_PR_LABEL_COUNT", "3");
        verify(envVars).put("SCMM_PR_LABEL_1", "backend");
        verify(envVars).put("SCMM_PR_LABEL_2", "needs review");
        verify(envVars).put("SCMM_PR_LABEL_3", "comma,label");
        verifyNoMoreInteractions(envVars);
    }

    @Test
    void shouldInjectEmptyLabelListForPullRequestWithoutLabels() {
        useBranch(pullRequestHead(List.of()));

        envContributor.buildEnvironmentFor(run, envVars, listener);

        verifyNoInteractions(apiFactory);
        verify(envVars).put("SCMM_PR_LABELS", "[]");
        verify(envVars).put("SCMM_PR_LABEL_COUNT", "0");
        verifyNoMoreInteractions(envVars);
    }

    @Test
    void shouldNotInjectLabelsForNonPullRequestBuild() {
        useBranch(new ScmManagerHead(CLONE_INFORMATION, "main"));

        envContributor.buildEnvironmentFor(run, envVars, listener);

        verifyNoInteractions(envVars);
    }

    @Test
    void shouldNotInjectLabelsWithoutBranchJobProperty() {
        envContributor.buildEnvironmentFor(run, envVars, listener);

        verifyNoInteractions(envVars);
    }

    private Repository setupApiCall(PullRequest pullRequest) {
        Repository repository = new Repository(NAMESPACE, NAME, "git");
        when(job.getAction(ScmManagerApiData.class))
                .thenReturn(new ScmManagerApiData(SERVER_URL, CREDENTIALS_ID, NAMESPACE, NAME));
        when(apiFactory.create(owner, SERVER_URL, CREDENTIALS_ID)).thenReturn(api);
        when(api.getRepository(NAMESPACE, NAME)).thenReturn(completedFuture(repository));
        when(api.getPullRequest(repository, "42")).thenReturn(completedFuture(pullRequest));
        return repository;
    }

    private ScmManagerPullRequestHead pullRequestHead(List<String> labels) {
        return new ScmManagerPullRequestHead(
                CLONE_INFORMATION,
                "42",
                new ScmManagerHead(CLONE_INFORMATION, "main"),
                new ScmManagerHead(CLONE_INFORMATION, "feature"),
                labels);
    }

    private PullRequest pullRequestWithLabels(String... labels) {
        PullRequest pullRequest = mock(PullRequest.class);
        when(pullRequest.getLabels()).thenReturn(List.of(labels));
        return pullRequest;
    }

    private void useBranch(SCMHead head) {
        when(job.getProperty(BranchJobProperty.class)).thenReturn(branchJobProperty);
        when(branchJobProperty.getBranch()).thenReturn(new Branch("source-id", head, mock(SCM.class), List.of()));
    }
}
