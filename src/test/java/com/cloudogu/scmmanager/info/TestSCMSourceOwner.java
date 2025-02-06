package com.cloudogu.scmmanager.info;

import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSourceOwner;
import org.jvnet.hudson.reactor.ReactorException;

import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.IOException;

public abstract class TestSCMSourceOwner extends Jenkins implements SCMSourceOwner {
    protected TestSCMSourceOwner(File root, ServletContext context) throws IOException, InterruptedException, ReactorException {
        super(root, context);
    }
}
