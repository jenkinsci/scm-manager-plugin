package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.Namespace;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.trait.SCMTrait;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.emptyList;

public class ScmManagerNavigator extends SCMNavigator {

  private final String serverUrl;
  private final String namespace;
  private final String credentialsId;

  @NonNull
  private List<SCMTrait<? extends SCMTrait<?>>> traits = new ArrayList<>();

  @NonNull
  private ScmManagerApiFactory apiFactory;

  public ScmManagerNavigator() {
    this(null, null, null);
  }

  @DataBoundConstructor
  public ScmManagerNavigator(String serverUrl, String namespace, String credentialsId) {
    this(serverUrl, namespace, credentialsId, new ScmManagerApiFactory());
  }

  public ScmManagerNavigator(String serverUrl, String namespace, String credentialsId, ScmManagerApiFactory apiFactory) {
    this.serverUrl = serverUrl;
    this.namespace = namespace;
    this.credentialsId = credentialsId;
    this.apiFactory = apiFactory;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getCredentialsId() {
    return credentialsId;
  }

  @Override
  public void setTraits(List<SCMTrait<? extends SCMTrait<?>>> traits) {
    this.traits = new ArrayList<>(Util.fixNull(traits));
  }

  @Override
  protected String id() {
    return serverUrl + "::" + namespace;
  }

  @Override
  public void visitSources(SCMSourceObserver observer) throws IOException, InterruptedException {
    TaskListener listener = observer.getListener();
  }

  @Override
  public List<SCMTrait<? extends SCMTrait<?>>> getTraits() {
    return Collections.unmodifiableList(traits);
  }

  @Extension
//  @Symbol("my")
  public static class DescriptorImpl extends SCMNavigatorDescriptor {

    private final ScmManagerApiFactory apiFactory;

    public DescriptorImpl() {
      this.apiFactory = new ScmManagerApiFactory();
    }

    @NonNull
    @Override
    public String getDisplayName() {
      return "SCM-Manager Namespace";
    }

    @Override
    public String getPronoun() {
      return "Repository";
    }

    @Override
    public ScmManagerNavigator newInstance(String name) {
      return new ScmManagerNavigator();
    }

    @SuppressWarnings("unused") // used By stapler
    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String value) {
      return ConnectionConfiguration.fillCredentialsIdItems(context, value);
    }

    @SuppressWarnings("unused") // used By stapler
    public FormValidation doCheckCredentialsId(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value) throws InterruptedException, ExecutionException {
      return ConnectionConfiguration.validateCredentialsId(apiFactory, context, serverUrl, value);
    }

    @SuppressWarnings("unused") // used By stapler
    public FormValidation doCheckServerUrl(@QueryParameter String value) throws InterruptedException, ExecutionException {
      return ConnectionConfiguration.checkServerUrl(apiFactory, value);
    }

    @SuppressWarnings("unused") // used By stapler
    public ListBoxModel doFillNamespaceItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String credentialsId, @QueryParameter String value) throws InterruptedException, ExecutionException {
      ListBoxModel model = new ListBoxModel();
      if (Strings.isNullOrEmpty(serverUrl) || Strings.isNullOrEmpty(credentialsId)) {
        if (!Strings.isNullOrEmpty(value)) {
          model.add(value);
        }
        return model;
      }

      ScmManagerApi api = apiFactory.create(context, serverUrl, credentialsId);
      api
        .getNamespaces()
        .exceptionally(e -> emptyList())
        .get()
        .stream()
        .map(Namespace::getNamespace)
        .sorted()
        .forEach(n -> model.add(new ListBoxModel.Option(n, n)));
      return model;
    }

    protected ListBoxModel.Option createNamespaceOption(Namespace namespace) {
      return new ListBoxModel.Option(namespace.getNamespace(), namespace.getNamespace());
    }
  }
}

