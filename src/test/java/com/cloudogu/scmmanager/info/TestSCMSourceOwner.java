package com.cloudogu.scmmanager.info;

import jakarta.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSourceOwner;
import org.jvnet.hudson.reactor.ReactorException;

public abstract class TestSCMSourceOwner extends Jenkins implements SCMSourceOwner {
    protected TestSCMSourceOwner(File root, ServletContext context)
            throws IOException, InterruptedException, ReactorException {
        super(root, context);
    }
}
