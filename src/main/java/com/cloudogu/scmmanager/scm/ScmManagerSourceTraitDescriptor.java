package com.cloudogu.scmmanager.scm;

import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;

public abstract class ScmManagerSourceTraitDescriptor extends SCMSourceTraitDescriptor {

    @Override
    public Class<? extends SCMSourceContext> getContextClass() {
        return ScmManagerSourceContext.class;
    }

    @Override
    public Class<? extends SCMSource> getSourceClass() {
        return ScmManagerSource.class;
    }
}
