package com.cloudogu.scmmanager.scm.env;

import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.util.List;
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

    @Override
    public void buildEnvironmentFor(@Nonnull Run run, @Nonnull EnvVars envs, @Nonnull TaskListener listener) {
        ScmManagerPullRequestHead pullRequestHead = getPullRequestHead(run);
        if (pullRequestHead == null) {
            return;
        }

        List<String> labels = pullRequestHead.getLabels();
        envs.put(ENV_LABELS, toJson(labels));
        envs.put(ENV_LABEL_COUNT, String.valueOf(labels.size()));

        for (int i = 0; i < labels.size(); i++) {
            envs.put(ENV_LABEL_PREFIX + (i + 1), labels.get(i));
        }
    }

    private ScmManagerPullRequestHead getPullRequestHead(Run run) {
        Object parent = run.getParent();
        if (!(parent instanceof Job)) {
            return null;
        }

        BranchJobProperty branchJobProperty = ((Job<?, ?>) parent).getProperty(BranchJobProperty.class);
        if (branchJobProperty == null) {
            return null;
        }

        Branch branch = branchJobProperty.getBranch();
        if (branch == null) {
            return null;
        }

        SCMHead head = branch.getHead();
        if (head instanceof ScmManagerPullRequestHead) {
            return (ScmManagerPullRequestHead) head;
        }

        return null;
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
