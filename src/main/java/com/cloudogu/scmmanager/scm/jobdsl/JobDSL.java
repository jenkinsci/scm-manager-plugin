package com.cloudogu.scmmanager.scm.jobdsl;

import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import jenkins.model.Jenkins;

public final class JobDSL {

    private JobDSL() {}

    public static <C extends ScmManagerContext> C resolve(Executor executor, Runnable closure, C context) {
        executor.executeInContext(closure, context);
        context.validate();
        return context;
    }

    public static ScmManagerApi createApi(ScmManagerApiFactory apiFactory, ScmManagerContext context) {
        return apiFactory.create(Jenkins.get(), context.getServerUrl(), context.getCredentialsId());
    }
}
