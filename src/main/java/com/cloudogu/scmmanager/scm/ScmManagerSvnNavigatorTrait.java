package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.scm.api.trait.SCMNavigatorContext;
import jenkins.scm.api.trait.SCMNavigatorTrait;
import jenkins.scm.api.trait.SCMNavigatorTraitDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class ScmManagerSvnNavigatorTrait extends SCMNavigatorTrait {

    private final String includes;
    private final String excludes;

    public ScmManagerSvnNavigatorTrait() {
        this(Subversion.DEFAULT_INCLUDES, Subversion.DEFAULT_EXCLUDES);
    }

    @DataBoundConstructor
    public ScmManagerSvnNavigatorTrait(String includes, String excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    @SuppressWarnings("unused") // used by stapler
    public String getIncludes() {
        return includes;
    }

    @SuppressWarnings("unused") // used by stapler
    public String getExcludes() {
        return excludes;
    }

    @Override
    protected void decorateContext(SCMNavigatorContext<?, ?> context) {
        if (context instanceof ScmManagerNavigatorContext) {
            ((ScmManagerNavigatorContext) context).setSvnIncludes(includes);
            ((ScmManagerNavigatorContext) context).setSvnExcludes(excludes);
        }
    }

    @Extension
    public static class DescriptorImpl extends SCMNavigatorTraitDescriptor {

        @SuppressWarnings("unused") // used by stapler
        public static final String DEFAULT_INCLUDES = Subversion.DEFAULT_INCLUDES;
        @SuppressWarnings("unused") // used by stapler
        public static final String DEFAULT_EXCLUDES = Subversion.DEFAULT_EXCLUDES;

        @Override
        public Class<? extends SCMNavigatorContext> getContextClass() {
            return ScmManagerNavigatorContext.class;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Subversion Discovery";
        }
    }
}
