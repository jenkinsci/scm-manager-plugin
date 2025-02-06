package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.plugins.mercurial.MercurialSCM;
import hudson.plugins.mercurial.traits.MercurialBrowserSCMSourceTrait;
import hudson.scm.SCM;
import java.util.Collection;
import java.util.stream.Collectors;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;

@Extension(optional = true)
public class MercurialSCMBuilderProvider extends SCMBuilderProvider {

    private static final String TYPE = "hg";
    private static final String DISPLAY_NAME = "Mercurial";

    public MercurialSCMBuilderProvider() {
        super(TYPE, DISPLAY_NAME);
    }

    @Override
    public Class<? extends SCM> getScmClass() {
        return MercurialSCM.class;
    }

    @Override
    public boolean isSupported(@NonNull SCMHeadCategory category) {
        return category.isUncategorized();
    }

    @Override
    public Collection<SCMSourceTraitDescriptor> getTraitDescriptors(SCMSourceDescriptor sourceDescriptor) {
        return SCMSourceTrait._for(null, null, ScmManagerHgSCMBuilder.class).stream()
                .filter(desc -> !(desc instanceof MercurialBrowserSCMSourceTrait.DescriptorImpl))
                .collect(Collectors.toList());
    }

    @Override
    protected SCMBuilder<?, ?> create(Context context) {
        return new ScmManagerHgSCMBuilder(context.getHead(), context.getRevision(), context.getCredentialsId());
    }
}
