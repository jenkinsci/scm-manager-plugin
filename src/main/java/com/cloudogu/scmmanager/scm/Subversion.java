package com.cloudogu.scmmanager.scm;

import jenkins.model.Jenkins;

public final class Subversion {
    public static final String DEFAULT_INCLUDES = "trunk,branches/*,tags/*,sandbox/*";
    public static final String DEFAULT_EXCLUDES = "";

    private Subversion() {}

    public static boolean isSupported() {
        return Jenkins.get().getPlugin("subversion") != null;
    }
}
