package com.cloudogu.scmmanager.scm.env;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import hudson.EnvVars;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import java.util.List;
import jenkins.branch.Branch;
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

    private static final CloneInformation CLONE_INFORMATION = new CloneInformation("git", "http://example.com/repo");

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
        lenient().when(run.getParent()).thenReturn(job);
        envContributor = new PullRequestLabelsEnvContributor();
    }

    @Test
    void shouldInjectPullRequestLabels() {
        useBranch(new ScmManagerPullRequestHead(
                CLONE_INFORMATION,
                "42",
                new ScmManagerHead(CLONE_INFORMATION, "main"),
                new ScmManagerHead(CLONE_INFORMATION, "feature"),
                List.of("backend", "needs review", "comma,label")));

        envContributor.buildEnvironmentFor(run, envVars, listener);

        verify(envVars).put("SCMM_PR_LABELS", "[\"backend\",\"needs review\",\"comma,label\"]");
        verify(envVars).put("SCMM_PR_LABEL_COUNT", "3");
        verify(envVars).put("SCMM_PR_LABEL_1", "backend");
        verify(envVars).put("SCMM_PR_LABEL_2", "needs review");
        verify(envVars).put("SCMM_PR_LABEL_3", "comma,label");
        verifyNoMoreInteractions(envVars);
    }

    @Test
    void shouldInjectEmptyLabelListForPullRequestWithoutLabels() {
        useBranch(new ScmManagerPullRequestHead(
                CLONE_INFORMATION,
                "42",
                new ScmManagerHead(CLONE_INFORMATION, "main"),
                new ScmManagerHead(CLONE_INFORMATION, "feature")));

        envContributor.buildEnvironmentFor(run, envVars, listener);

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

    private void useBranch(SCMHead head) {
        when(job.getProperty(BranchJobProperty.class)).thenReturn(branchJobProperty);
        when(branchJobProperty.getBranch()).thenReturn(new Branch("source-id", head, mock(SCM.class), List.of()));
    }
}
