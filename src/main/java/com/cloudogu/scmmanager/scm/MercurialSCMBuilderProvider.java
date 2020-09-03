package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.plugins.mercurial.traits.MercurialBrowserSCMSourceTrait;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;

import java.util.Collection;
import java.util.stream.Collectors;

@Extension(optional = true)
public class MercurialSCMBuilderProvider extends SCMBuilderProvider {

  private static final String TYPE = "hg";

  public MercurialSCMBuilderProvider() {
    super(TYPE);
  }

  @Override
  public boolean isSupported(@NonNull SCMHeadCategory category) {
    return category.isUncategorized();
  }

  @Override
  public Collection<SCMSourceTraitDescriptor> getTraitDescriptors(SCMSourceDescriptor sourceDescriptor) {
    return SCMSourceTrait._for(sourceDescriptor, null, ScmManagerHgSCMBuilder.class)
      .stream()
      .filter(desc -> !(desc instanceof MercurialBrowserSCMSourceTrait.DescriptorImpl))
      .collect(Collectors.toList());
  }

  @Override
  protected SCMBuilder<?, ?> create(Context context) {
    return new ScmManagerHgSCMBuilder(
      context.getHead(),
      context.getRevision(),
      context.getCredentialsId()
    );
  }
}
