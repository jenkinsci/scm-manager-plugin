package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.Repository;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;

public abstract class ScmManagerSourceTraitDescriptor extends SCMSourceTraitDescriptor {

  public boolean isApplicableToRepository(Repository repository) {
    return false;
  }

}
