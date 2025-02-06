package com.cloudogu.scmmanager.info;

import hudson.model.ItemGroup;
import hudson.model.Job;

public abstract class TestJob extends Job<TestJob, TestRun> {
    protected TestJob(ItemGroup parent, String name) {
        super(parent, name);
    }
}
