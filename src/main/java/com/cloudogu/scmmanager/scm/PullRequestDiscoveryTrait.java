package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.Repository;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.trait.Discovery;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class PullRequestDiscoveryTrait extends SCMSourceTrait {

  @DataBoundConstructor
  public PullRequestDiscoveryTrait() {
  }

  @Override
  protected void decorateContext(SCMSourceContext<?, ?> context) {
    ScmManagerSourceContext scmContext = (ScmManagerSourceContext) context;
    scmContext.wantPullRequests(true);
  }

  @Override
  protected boolean includeCategory(@NonNull SCMHeadCategory category) {
    return category instanceof ChangeRequestSCMHeadCategory;
  }


  @Extension
  @Discovery
  public static class DescriptorImpl extends ScmManagerSourceTraitDescriptor {

    @Nonnull
    @Override
    public String getDisplayName() {
      return "Pull Request Discovery";
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
      return repository.getLinks().hasLink("pullRequest");
    }
  }
}
