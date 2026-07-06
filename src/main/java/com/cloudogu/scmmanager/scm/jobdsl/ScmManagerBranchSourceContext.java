package com.cloudogu.scmmanager.scm.jobdsl;

import com.cloudogu.scmmanager.scm.PullRequestDiscoveryTrait;
import com.cloudogu.scmmanager.scm.ScmManagerBranchDiscoveryTrait;
import com.cloudogu.scmmanager.scm.TagDiscoveryTrait;
import java.util.ArrayList;
import java.util.List;
import jenkins.scm.api.trait.SCMSourceTrait;

public class ScmManagerBranchSourceContext extends BranchSourceContext {

    private boolean discoverBranches = true;
    private boolean discoverPullRequest = true;
    private boolean excludeDraftPullRequests = false;
    private boolean discoverTags = false;

    public void discoverBranches(boolean discoverBranches) {
        this.discoverBranches = discoverBranches;
    }

    public void discoverPullRequest(boolean discoverPullRequest) {
        this.discoverPullRequest = discoverPullRequest;
    }

    public void excludeDraftPullRequests(boolean excludeDraftPullRequests) {
        this.excludeDraftPullRequests = excludeDraftPullRequests;
    }

    public void discoverTags(boolean discoverTags) {
        this.discoverTags = discoverTags;
    }

    public List<SCMSourceTrait> getTraits() {
        List<SCMSourceTrait> traits = new ArrayList<>();
        if (discoverBranches) {
            traits.add(new ScmManagerBranchDiscoveryTrait());
        }
        if (discoverPullRequest) {
            traits.add(new PullRequestDiscoveryTrait(false, excludeDraftPullRequests));
        }
        if (discoverTags) {
            traits.add(new TagDiscoveryTrait());
        }
        return traits;
    }
}
