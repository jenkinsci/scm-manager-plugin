package com.cloudogu.scmmanager.info;

import hudson.model.Run;

import javax.annotation.Nonnull;
import java.io.IOException;

public abstract class TestRun extends Run<TestJob, TestRun> {
  protected TestRun(@Nonnull TestJob job) throws IOException {
    super(job);
  }
}
