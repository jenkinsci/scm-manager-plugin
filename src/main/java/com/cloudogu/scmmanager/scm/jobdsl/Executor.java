package com.cloudogu.scmmanager.scm.jobdsl;

import javaposse.jobdsl.dsl.Context;

@FunctionalInterface
interface Executor {
    void executeInContext(Runnable runnable, Context context);
}
