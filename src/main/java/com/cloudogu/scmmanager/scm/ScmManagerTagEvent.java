package com.cloudogu.scmmanager.scm;

import static java.util.stream.Collectors.toList;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.ScmManagerTag;
import java.util.Collection;
import jenkins.scm.api.SCMHead;
import net.sf.json.JSONObject;

public class ScmManagerTagEvent extends ScmManagerHeadEvent {

    private Collection<String> names;

    ScmManagerTagEvent(Type type, JSONObject form, Collection<JSONObject> tags) {
        super(type, form);
        this.names = tags.stream().map(tag -> tag.getString("name")).collect(toList());
    }

    @Override
    Collection<SCMHead> heads(CloneInformation cloneInformation) {
        return names.stream()
                .map(name -> new ScmManagerTag(cloneInformation, name, System.currentTimeMillis()))
                .collect(toList());
    }
}
