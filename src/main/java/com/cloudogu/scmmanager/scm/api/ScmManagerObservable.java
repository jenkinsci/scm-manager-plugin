package com.cloudogu.scmmanager.scm.api;

import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;

public interface ScmManagerObservable {
    SCMHead head();

    SCMRevision revision();
}
