package com.cloudogu.scmmanager.scm;

import static java.util.stream.Collectors.toList;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import java.util.Collection;
import jenkins.scm.api.SCMHead;
import net.sf.json.JSONObject;

public class ScmManagerBranchEventFromPullRequest extends ScmManagerHeadEvent {

    private Collection<String> names;

    ScmManagerBranchEventFromPullRequest(Type type, JSONObject form, Collection<JSONObject> pullRequests) {
        super(correspondingTypeForBranch(type), form);
        this.names =
                pullRequests.stream().map(branch -> branch.getString("source")).collect(toList());
    }

    private static Type correspondingTypeForBranch(Type type) {
        switch (type) {
            case CREATED:
            case UPDATED:
                // When a pull request is created or changed, the corresponding branch may be to be removed
                return Type.REMOVED;
            case REMOVED:
                // When a pul request is removed, the corresponding branch may be to be build again
                return Type.CREATED;
            default:
                // The most secure way:
                return Type.CREATED;
        }
    }

    @Override
    Collection<SCMHead> heads(CloneInformation cloneInformation) {
        return names.stream()
                .map(name -> new ScmManagerHead(cloneInformation, name))
                .collect(toList());
    }
}
