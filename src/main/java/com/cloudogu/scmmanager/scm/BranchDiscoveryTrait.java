package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.impl.trait.Discovery;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * @deprecated This class has been replaced with the {@link ScmManagerBranchDiscoveryTrait}. The
 * name of this class has not been unique and this led to issues in the automated generation of
 * build jobs.
 *
 * @see ScmManagerBranchDiscoveryTrait
 */
@Deprecated
public class BranchDiscoveryTrait extends SCMSourceTrait {

  @DataBoundConstructor
  public BranchDiscoveryTrait() {
  }

  @Override
  protected void decorateContext(SCMSourceContext<?, ?> context) {
    ScmManagerSourceContext scmContext = (ScmManagerSourceContext) context;
    scmContext.wantBranches(true);
  }

  @Override
  protected boolean includeCategory(@NonNull SCMHeadCategory category) {
    return category.isUncategorized();
  }

  @Extension
  @Discovery
  public static class DescriptorImpl extends ScmManagerSourceTraitDescriptor {

    @Nonnull
    @Override
    public String getDisplayName() {
      return "Deprecated SCM-Manager Branch Discovery";
    }
  }
}
