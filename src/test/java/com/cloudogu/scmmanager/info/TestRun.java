package com.cloudogu.scmmanager.info;

import hudson.model.Run;
import java.io.IOException;
import javax.annotation.Nonnull;

public abstract class TestRun extends Run<TestJob, TestRun> {
    protected TestRun(@Nonnull TestJob job) throws IOException {
        super(job);
    }
}
