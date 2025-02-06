package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerTag;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.plugins.git.GitSCM;
import java.util.Arrays;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.plugins.git.MergeWithGitSCMExtension;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;

public class ScmManagerGitSCMBuilder extends GitSCMBuilder<ScmManagerGitSCMBuilder> {

    public ScmManagerGitSCMBuilder(
            @NonNull LinkBuilder linkBuilder,
            @NonNull ScmManagerHead head,
            SCMRevision revision,
            String credentialsId) {
        super(head, revision, head.getCloneInformation().getUrl(), credentialsId);
        // clean up
        withoutRefSpecs();

        withBrowser(new ScmManagerGitRepositoryBrowser(linkBuilder));

        if (head instanceof ScmManagerTag) {
            withRefSpec("+refs/tags/" + head.getName() + ":refs/tags/" + head.getName());
        } else if (head instanceof ScmManagerPullRequestHead) {
            ScmManagerPullRequestHead prHead = (ScmManagerPullRequestHead) head;

            ScmManagerHead source = prHead.getSource();
            ScmManagerHead target = prHead.getTarget();

            withRefSpecs(Arrays.asList(
                    "+refs/heads/" + source.getName() + ":refs/remotes/origin/" + source.getName(),
                    "+refs/heads/" + target.getName() + ":refs/remotes/origin/" + target.getName()));

            // revision is null on initial build
            if (revision != null) {
                ScmManagerPullRequestRevision prRevision = (ScmManagerPullRequestRevision) revision;
                withRevision(prRevision.getSourceRevision());
            }
        } else {
            withRefSpec("+refs/heads/" + head.getName() + ":refs/remotes/@{remote}/" + head.getName());
        }
    }

    @Override
    public GitSCM build() {
        SCMHead head = head();
        if (head instanceof ScmManagerPullRequestHead) {
            ScmManagerPullRequestHead pr = (ScmManagerPullRequestHead) head;
            if (pr.getCheckoutStrategy() == ChangeRequestCheckoutStrategy.MERGE) {
                configureMerge(pr);
            } else {
                withHead(pr.getTarget());
            }
        }

        return super.build();
    }

    private void configureMerge(ScmManagerPullRequestHead pr) {
        withHead(pr.getTarget());
        withExtension(
                new MergeWithGitSCMExtension("remotes/origin/" + pr.getSource().getName(), getBaseHash()));
    }

    private String getBaseHash() {
        SCMRevision revision = revision();
        if (revision instanceof ScmManagerPullRequestRevision) {
            SCMRevision rev = ((ScmManagerPullRequestRevision) revision).getSourceRevision();
            return rev.toString();
        }
        return null;
    }
}
