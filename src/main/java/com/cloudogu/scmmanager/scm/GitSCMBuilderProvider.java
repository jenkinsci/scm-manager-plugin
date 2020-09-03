package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import jenkins.plugins.git.traits.GitBrowserSCMSourceTrait;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;

import java.util.Collection;
import java.util.stream.Collectors;

@Extension(optional = true)
public class GitSCMBuilderProvider extends SCMBuilderProvider {

  private static final String TYPE = "git";
  private static final String DISPLAY_NAME = "Git";

  public GitSCMBuilderProvider() {
    super(TYPE, DISPLAY_NAME);
  }

  @Override
  public boolean isSupported(@NonNull SCMHeadCategory category) {
    return true;
  }

  @Override
  public Class<? extends SCM> getScmClass() {
    return GitSCM.class;
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
