package com.cloudogu.scmmanager.scm.env;

import com.cloudogu.scmmanager.scm.ScmManagerApiData;
import com.cloudogu.scmmanager.scm.api.PullRequest;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import jenkins.branch.Branch;
import jenkins.scm.api.SCMHead;
import lombok.extern.slf4j.Slf4j;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

@Extension(optional = true)
@Slf4j
public class PullRequestLabelsEnvContributor extends EnvironmentContributor {

    private static final String ENV_LABELS = "SCMM_PR_LABELS";
    private static final String ENV_LABEL_COUNT = "SCMM_PR_LABEL_COUNT";
    private static final String ENV_LABEL_PREFIX = "SCMM_PR_LABEL_";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ScmManagerApiFactory apiFactory;

    public PullRequestLabelsEnvContributor() {
        this(new ScmManagerApiFactory());
    }

    PullRequestLabelsEnvContributor(ScmManagerApiFactory apiFactory) {
        this.apiFactory = apiFactory;
    }

    @Override
    public void buildEnvironmentFor(@Nonnull Run run, @Nonnull EnvVars envs, @Nonnull TaskListener listener) {
        ScmManagerPullRequestHead pullRequestHead = getPullRequestHead(run);
        if (pullRequestHead == null) {
            return;
        }

        List<String> labels = fetchLabels(run, pullRequestHead);
        envs.put(ENV_LABELS, toJson(labels));
        envs.put(ENV_LABEL_COUNT, String.valueOf(labels.size()));

        for (int i = 0; i < labels.size(); i++) {
            envs.put(ENV_LABEL_PREFIX + (i + 1), labels.get(i));
        }
    }

    private ScmManagerPullRequestHead getPullRequestHead(Run run) {
        Job<?, ?> parent = run.getParent();
        BranchJobProperty branchJobProperty = parent.getProperty(BranchJobProperty.class);
        if (branchJobProperty == null) {
            return null;
        }

        Branch branch = branchJobProperty.getBranch();
        SCMHead head = branch.getHead();
        if (head instanceof ScmManagerPullRequestHead) {
            return (ScmManagerPullRequestHead) head;
        }

        return null;
    }

    private List<String> fetchLabels(Run run, ScmManagerPullRequestHead pullRequestHead) {
        Job<?, ?> job = run.getParent();
        ScmManagerApiData apiData = job.getAction(ScmManagerApiData.class);
        if (apiData == null) {
            return pullRequestHead.getLabels();
        }

        try {
            ScmManagerApi client = apiFactory.create(
                    (ItemGroup<?>) job.getParent(), apiData.getServerUrl(), apiData.getCredentialsId());
            Repository repository = client.getRepository(apiData.getNamespace(), apiData.getName())
                    .get();
            CompletableFuture<PullRequest> pullRequest = client.getPullRequest(repository, pullRequestHead.getId());
            if (pullRequest == null) {
                return pullRequestHead.getLabels();
            }
            return pullRequest.get().getLabels();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("could not fetch labels for pull request {}. Error: {}", pullRequestHead.getId(), e.getMessage());
        } catch (ExecutionException e) {
            log.error("could not fetch labels for pull request {}. Error: {}", pullRequestHead.getId(), e.getMessage());
        }

        return pullRequestHead.getLabels();
    }

    private String toJson(List<String> labels) {
        try {
            return OBJECT_MAPPER.writeValueAsString(labels);
        } catch (JsonProcessingException e) {
            log.warn("could not serialize SCM-Manager pull request labels", e);
            return "[]";
        }
    }
}
