package com.cloudogu.scmmanager.scm.jobdsl;

import com.cloudogu.scmmanager.scm.ScmManagerSource;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.DslContext;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.workflow.BranchSourcesContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;
import jenkins.branch.BranchSource;
import jenkins.model.Jenkins;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Extension(optional = true)
public class BranchSourcesExtension extends ContextExtensionPoint {

  private final ScmManagerApiFactory apiFactory;
  private final Executor executor;

  public BranchSourcesExtension() {
    this.apiFactory = new ScmManagerApiFactory();
    this.executor = BranchSourcesExtension::executeInContext;
  }

  @VisibleForTesting
  BranchSourcesExtension(ScmManagerApiFactory apiFactory, Executor executor) {
    this.apiFactory = apiFactory;
    this.executor = executor;
  }

  @RequiresPlugin(id = "scm-manager-plugin")
  @DslExtensionMethod(context = BranchSourcesContext.class)
  public BranchSource scmManager(@DslContext(ScmManagerBranchSourceContext.class) Runnable closure) throws ExecutionException, InterruptedException {
    ScmManagerBranchSourceContext context = new ScmManagerBranchSourceContext();
    executor.executeInContext(closure, context);
    context.validate();

    String repository = resolveRepository(context);

    ScmManagerSource source = new ScmManagerSource(
      context.getServerUrl(),
      repository,
      context.getCredentialsId()
    );
    source.setId(context.getId());
    source.setTraits(context.getTraits());

    return new BranchSource(source);
  }

  private String resolveRepository(ScmManagerBranchSourceContext context) throws ExecutionException, InterruptedException {
    String repository = context.getRepository();
    String[] parts = repository.split("/");
    if (parts.length < 3) {
      repository = getRepositoryIdFromRemote(context, parts[0], parts[1]);
    }
    return repository;
  }

  private String getRepositoryIdFromRemote(ScmManagerBranchSourceContext context, String namespace, String name) throws InterruptedException, java.util.concurrent.ExecutionException {
    ScmManagerApi api = createApi(context);
    CompletableFuture<Repository> future = api.getRepository(namespace, name);
    String type = future.get().getType();
    return String.format("%s/%s/%s", namespace, name, type);
  }

  private ScmManagerApi createApi(ScmManagerBranchSourceContext context) {
    return apiFactory.create(Jenkins.get(), context.getServerUrl(), context.getCredentialsId());
  }

  @FunctionalInterface
  interface Executor {

    void executeInContext(Runnable runnable, Context context);

  }

}
