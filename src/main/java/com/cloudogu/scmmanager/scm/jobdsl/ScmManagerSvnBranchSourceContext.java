package com.cloudogu.scmmanager.scm.jobdsl;

import com.cloudogu.scmmanager.scm.Subversion;

public class ScmManagerSvnBranchSourceContext extends BranchSourceContext {

  private String includes = Subversion.DEFAULT_INCLUDES;
  private String excludes = Subversion.DEFAULT_EXCLUDES;

  public void includes(String includes) {
    this.includes = includes;
  }

  public String getIncludes() {
    return includes;
  }

  public void excludes(String excludes) {
    this.excludes = excludes;
  }

  public String getExcludes() {
    return excludes;
  }
}
