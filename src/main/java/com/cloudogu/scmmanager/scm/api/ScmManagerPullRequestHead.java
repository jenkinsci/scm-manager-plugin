package com.cloudogu.scmmanager.scm.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;

public class ScmManagerPullRequestHead extends ScmManagerHead implements ChangeRequestSCMHead2 {

    private static final long serialVersionUID = 1L;

    @NonNull
    private final String id;

    private final ScmManagerHead target;
    private final ScmManagerHead source;
    private final List<String> labels;

    public ScmManagerPullRequestHead(
            @NonNull CloneInformation cloneInformation,
            @NonNull String id,
            @NonNull ScmManagerHead target,
            ScmManagerHead source) {
        this(cloneInformation, id, target, source, Collections.emptyList());
    }

    public ScmManagerPullRequestHead(
            @NonNull CloneInformation cloneInformation,
            @NonNull String id,
            @NonNull ScmManagerHead target,
            ScmManagerHead source,
            List<String> labels) {
        // ?? why PullRequest/...
        super(cloneInformation, "PR-" + id);
        this.id = id;
        this.target = target;
        this.source = source;
        this.labels = labels == null ? Collections.emptyList() : new ArrayList<>(labels);
    }

    @NonNull
    @Override
    public String getId() {
        return id;
    }

    @NonNull
    @Override
    public ScmManagerHead getTarget() {
        return target;
    }

    public ScmManagerHead getSource() {
        return source;
    }

    public List<String> getLabels() {
        if (labels == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(labels);
    }

    @NonNull
    @Override
    public ChangeRequestCheckoutStrategy getCheckoutStrategy() {
        return ChangeRequestCheckoutStrategy.MERGE;
    }

    @NonNull
    @Override
    public String getOriginName() {
        return getSource().getName();
    }

    @NonNull
    @Override
    public SCMHeadOrigin getOrigin() {
        return SCMHeadOrigin.DEFAULT;
    }
}
