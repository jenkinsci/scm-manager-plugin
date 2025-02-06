package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerTag;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.trait.SCMHeadFilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.trait.Discovery;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class PullRequestDiscoveryTrait extends SCMSourceTrait {

    private final boolean excludeBranchesWithPRs;

    @DataBoundConstructor
    public PullRequestDiscoveryTrait(boolean excludeBranchesWithPRs) {
        this.excludeBranchesWithPRs = excludeBranchesWithPRs;
    }

    /**
     * Constructor for old versions of this trait, which does not have the excludeBranchesWithPRs option.
     */
    public PullRequestDiscoveryTrait() {
        this(false);
    }

    public boolean isExcludeBranchesWithPRs() {
        return excludeBranchesWithPRs;
    }

    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        ScmManagerSourceContext scmContext = (ScmManagerSourceContext) context;
        scmContext.wantPullRequests(true);
        if (excludeBranchesWithPRs) {
            scmContext.withFilter(new ExcludePullRequestBranchHeadFilter());
        }
    }

    @Override
    protected boolean includeCategory(@NonNull SCMHeadCategory category) {
        return category instanceof ChangeRequestSCMHeadCategory;
    }

    @Extension
    @Discovery
    public static class DescriptorImpl extends ScmManagerSourceTraitDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Pull Request Discovery";
        }
    }

    @VisibleForTesting
    static class ExcludePullRequestBranchHeadFilter extends SCMHeadFilter {

        @Override
        @SuppressFBWarnings("BC_UNCONFIRMED_CAST") // type is checked isBranchHead
        public boolean isExcluded(@NonNull SCMSourceRequest request, @NonNull SCMHead head) {
            if (request instanceof ScmManagerSourceRequest && isBranchHead(head)) {
                ScmManagerSourceRequest req = (ScmManagerSourceRequest) request;
                ScmManagerHead branch = (ScmManagerHead) head;

                for (ScmManagerPullRequestHead pullRequest : req.getPullRequests()) {
                    String source = pullRequest.getSource().getName();
                    if (source.equals(branch.getName())) {
                        return true;
                    }
                }

            }
            return false;
        }

        private boolean isBranchHead(@Nonnull SCMHead head) {
            return head instanceof ScmManagerHead
                && !(head instanceof ScmManagerTag)
                && !(head instanceof ScmManagerPullRequestHead);
        }
    }
}
