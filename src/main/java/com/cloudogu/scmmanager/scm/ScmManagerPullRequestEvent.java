package com.cloudogu.scmmanager.scm;

import static java.util.stream.Collectors.toList;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import java.util.Collection;
import jenkins.scm.api.SCMHead;
import net.sf.json.JSONObject;

public class ScmManagerPullRequestEvent extends ScmManagerHeadEvent {

    private Collection<PullRequestFromJson> pullRequests;

    public ScmManagerPullRequestEvent(Type type, JSONObject form, Collection<JSONObject> pullRequests) {
        super(type, form);
        this.pullRequests = pullRequests.stream().map(PullRequestFromJson::new).collect(toList());
    }

    @Override
    Collection<SCMHead> heads(CloneInformation cloneInformation) {
        return pullRequests.stream()
                .map(pullRequest -> new ScmManagerPullRequestHead(
                        cloneInformation,
                        pullRequest.id,
                        new ScmManagerHead(cloneInformation, pullRequest.target),
                        new ScmManagerHead(cloneInformation, pullRequest.source)))
                .collect(toList());
    }

    private static class PullRequestFromJson {
        private final String id;
        private final String source;
        private final String target;

        public PullRequestFromJson(JSONObject json) {
            this.id = json.getString("id");
            this.source = json.getString("source");
            this.target = json.getString("target");
        }
    }
}
