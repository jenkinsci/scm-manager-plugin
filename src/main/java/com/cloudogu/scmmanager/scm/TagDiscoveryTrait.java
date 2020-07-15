package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.Repository;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.impl.TagSCMHeadCategory;
import jenkins.scm.impl.trait.Discovery;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class TagDiscoveryTrait extends SCMSourceTrait {

  @DataBoundConstructor
  public TagDiscoveryTrait() {
  }

  @Override
  protected void decorateContext(SCMSourceContext<?, ?> context) {
    ScmManagerSourceContext scmContext = (ScmManagerSourceContext) context;
    scmContext.wantTags(true);
  }

  @Override
  protected boolean includeCategory(@NonNull SCMHeadCategory category) {
    return category instanceof TagSCMHeadCategory;
  }


  @Extension
  @Discovery
  public static class DescriptorImpl extends ScmManagerSourceTraitDescriptor {

    @Nonnull
    @Override
    public String getDisplayName() {
      return "Tag Discovery";
    }

    @Override
    public Class<? extends SCMSourceContext> getContextClass() {
      return ScmManagerSourceContext.class;
    }

    @Override
    public Class<? extends SCMSource> getSourceClass() {
      return ScmManagerSource.class;
    }

    @Override
    public boolean isApplicableToRepository(Repository repository) {
      return repository.getLinks().hasLink("tags");
    }
  }
}
