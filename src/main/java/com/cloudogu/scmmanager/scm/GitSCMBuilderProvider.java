package com.cloudogu.scmmanager.scm;

import hudson.Extension;
import jenkins.plugins.git.traits.GitBrowserSCMSourceTrait;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;

import java.util.Collection;
import java.util.stream.Collectors;

@Extension(optional = true)
public class GitSCMBuilderProvider extends SCMBuilderProvider {

  private static final String TYPE = "git";

  public GitSCMBuilderProvider() {
    super(TYPE);
  }

  @Override
  public Collection<SCMSourceTraitDescriptor> getTraitDescriptors(SCMSourceDescriptor sourceDescriptor) {
    return SCMSourceTrait._for(sourceDescriptor, null, ScmManagerGitSCMBuilder.class)
      .stream()
      .filter(desc -> !(desc instanceof GitBrowserSCMSourceTrait.DescriptorImpl))
      .collect(Collectors.toList());
  }

  @Override
  protected SCMBuilder<?, ?> create(Context context) {
    return new ScmManagerGitSCMBuilder(
      context.getLinkBuilder(),
      context.getHead(),
      context.getRevision(),
      context.getCredentialsId()
    );
  }
}
