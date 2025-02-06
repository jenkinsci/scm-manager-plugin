package com.cloudogu.scmmanager.scm.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.mixin.TagSCMHead;

public class ScmManagerTag extends ScmManagerHead implements TagSCMHead {

    private final long timestamp;

    public ScmManagerTag(@NonNull CloneInformation cloneInformation, @NonNull String name, long timestamp) {
        super(cloneInformation, name);
        this.timestamp = timestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
