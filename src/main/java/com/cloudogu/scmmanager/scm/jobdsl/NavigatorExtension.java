package com.cloudogu.scmmanager.scm.jobdsl;

import com.cloudogu.scmmanager.scm.ScmManagerNavigator;
import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import javaposse.jobdsl.dsl.DslContext;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.workflow.ScmNavigatorsContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

import static com.cloudogu.scmmanager.scm.jobdsl.JobDSL.resolve;

@Extension(optional = true)
public class NavigatorExtension extends ContextExtensionPoint {

  private final Executor executor;

  public NavigatorExtension() {
    this.executor = ContextExtensionPoint::executeInContext;
  }

  @VisibleForTesting
  NavigatorExtension(Executor executor) {
    this.executor = executor;
  }

  @RequiresPlugin(id = "scm-manager")
  @DslExtensionMethod(context = ScmNavigatorsContext.class)
  public ScmManagerNavigator scmManagerNamespace(@DslContext(ScmManagerNavigatorContext.class) Runnable closure) {
    ScmManagerNavigatorContext context = resolve(executor, closure, new ScmManagerNavigatorContext(executor));
    ScmManagerNavigator navigator = new ScmManagerNavigator(
      context.getNamespace(), context.getServerUrl(), context.getNamespace(), context.getCredentialsId()
    );
    navigator.setTraits(context.getTraits());
    return navigator;
  }
}
