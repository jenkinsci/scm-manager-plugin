package com.cloudogu.scmmanager.scm;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudogu.scmmanager.scm.api.ExecutionExceptions;
import com.cloudogu.scmmanager.scm.api.Namespace;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCategory;
import jenkins.scm.api.SCMSourceEvent;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.trait.SCMNavigatorRequest;
import jenkins.scm.api.trait.SCMNavigatorTrait;
import jenkins.scm.api.trait.SCMNavigatorTraitDescriptor;
import jenkins.scm.api.trait.SCMTrait;
import jenkins.scm.api.trait.SCMTraitDescriptor;
import jenkins.scm.impl.UncategorizedSCMSourceCategory;
import jenkins.scm.impl.form.NamedArrayList;
import jenkins.util.NonLocalizable;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class ScmManagerNavigator extends SCMNavigator {

  public static final String ALL_NAMESPACES_LABEL = "--all--";

  private static final Predicate<String> DEFAULT_DEPENDENCY_CHECKER = plugin -> Jenkins.get().getPlugin(plugin) != null;

  private static final Logger LOG = LoggerFactory.getLogger(ScmManagerNavigator.class);

  private final String projectName;
  private final String serverUrl;
  private final String namespace;
  private final String credentialsId;
  private Predicate<String> dependencyChecker = DEFAULT_DEPENDENCY_CHECKER;

  @NonNull
  private List<SCMTrait<? extends SCMTrait<?>>> traits = new ArrayList<>();

  @NonNull
  private ScmManagerApiFactory apiFactory;

  public ScmManagerNavigator(String projectName) {
    this(projectName, null, null, null);
  }

  @DataBoundConstructor
  public ScmManagerNavigator(String projectName, String serverUrl, String namespace, String credentialsId) {
    this(projectName, serverUrl, namespace, credentialsId, DEFAULT_DEPENDENCY_CHECKER, new ScmManagerApiFactory());
  }

  public ScmManagerNavigator(String projectName, String serverUrl, String namespace, String credentialsId, Predicate<String> dependencyChecker, ScmManagerApiFactory apiFactory) {
    this.projectName = projectName;
    this.serverUrl = serverUrl;
    this.namespace = namespace;
    this.credentialsId = credentialsId;
    this.dependencyChecker = dependencyChecker;
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
  protected String id() {
    return Joiner.on("::").skipNulls().join(projectName, serverUrl, namespace);
  }

  @Override
  public List<SCMTrait<? extends SCMTrait<?>>> getTraits() {
    return Collections.unmodifiableList(traits);
  }

  @Override
  @DataBoundSetter
  public void setTraits(List<SCMTrait<? extends SCMTrait<?>>> traits) {
    this.traits = new ArrayList<>(Util.fixNull(traits));
  }

  @Override
  public void visitSources(SCMSourceObserver observer) throws IOException, InterruptedException {
    LOG.debug("Visiting sources with observer {}", observer);
    TaskListener listener = observer.getListener();
    try (ScmManagerNavigatorRequest request = new ScmManagerNavigatorContext()
      .withTraits(traits)
      .newRequest(this, observer)) {

      ScmManagerApi api = apiFactory.create(observer.getContext(), serverUrl, credentialsId);
      try {
        CompletableFuture<List<Repository>> repositoryRequest;
        if (isForAllNamespaces()) {
          repositoryRequest = api.getRepositories();
        } else {
          repositoryRequest = api.getRepositories(namespace);
        }
        List<Repository> repositories = repositoryRequest.get()
          .stream().filter(filterUnsupportedRepositories())
          .collect(Collectors.toList());
        for (Repository repository : repositories) {
          final String subProjectName;
          if (isForAllNamespaces()) {
            subProjectName = repository.getNamespace() + "/" + repository.getName();
          } else {
            subProjectName = repository.getName();
          }
          if (request.process(subProjectName, new ScmManagerSourceFactory(request, repository), null, new NavigatorWitness(listener))) {
            // the observer has seen enough and doesn't want to see anymore
            return;
          }
        }
      } catch (ExecutionException e) {
        ExecutionExceptions.log(e);
        // We have to throw an IOException here to prevent Jenkins
        // from removing all previously found repositories
        throw new IOException("failed to load repositories from SCM-Manager", e);
      }
    }
  }

  @Override
  public void visitSources(SCMSourceObserver observer, SCMSourceEvent<?> event) throws IOException, InterruptedException {
    if (event instanceof ScmManagerSourceEvent && ((ScmManagerSourceEvent) event).getPayload().isGlobal()) {
      visitSources(observer);
    } else {
      super.visitSources(observer, event);
    }
  }

  private Predicate<Repository> filterUnsupportedRepositories() {
    List<String> supportedTypes = supportedTypes();
    return repository -> supportedTypes.contains(repository.getType());
  }

  private List<String> supportedTypes() {
    List<String> types = new ArrayList<>();
    if (getDependencyChecker().test("git")) {
      types.add("git");
    }
    if (getDependencyChecker().test("mercurial")) {
      types.add("hg");
    }
    if (getDependencyChecker().test("subversion") && isSubversionTraitEnabled()) {
      types.add("svn");
    }
    return Collections.unmodifiableList(types);
  }

  private Predicate<String> getDependencyChecker() {
    return ofNullable(dependencyChecker).orElse(DEFAULT_DEPENDENCY_CHECKER);
  }

  private boolean isSubversionTraitEnabled() {
    return getTraits().stream().anyMatch(trait -> trait instanceof ScmManagerSvnNavigatorTrait);
  }

  public boolean isForNamespace(String namespace) {
    return isForAllNamespaces() || this.namespace.equals(namespace);
  }

  private boolean isForAllNamespaces() {
    return ALL_NAMESPACES_LABEL.equals(namespace);
  }

  class ScmManagerSourceFactory implements SCMNavigatorRequest.SourceLambda {

    private final ScmManagerNavigatorRequest request;
    private final Repository repository;

    public ScmManagerSourceFactory(ScmManagerNavigatorRequest request, Repository repository) {
      this.request = request;
      this.repository = repository;
    }

    @NonNull
    @Override
    public SCMSource create(@NonNull String projectName) {
      String repoId = Joiner.on("/").join(
        repository.getNamespace(), repository.getName(), repository.getType()
      );
      String id = getId() + "::" + repoId;
      if ("svn".equals(repository.getType())) {
        return createSvnSource(projectName, id);
      }
      return createSource(projectName, id, repoId);
    }

    private SCMSource createSvnSource(String projectName, String id) {
      return new ScmManagerSvnSourceBuilder(projectName, serverUrl, repository.mustGetUrl("http"), credentialsId)
        .withId(id)
        .withRequest(request)
        .withIncludes(request.getSvnIncludes())
        .withExcludes(request.getSvnExcludes())
        .build();
    }

    private SCMSource createSource(String projectName, String id, String repoId) {
      return new ScmManagerSourceBuilder(projectName, serverUrl, repoId, credentialsId)
        .withId(id)
        .withRequest(request)
        .build();
    }
  }

  @Extension
  @Symbol("scmManager")
  public static class DescriptorImpl extends SCMNavigatorDescriptor {

    private final ScmManagerApiFactory apiFactory;

    @Inject
    private ScmManagerSource.DescriptorImpl delegate;

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
      return "Namespace";
    }

    static {
      Icons.register("icon-scm-manager-navigator");
    }

    @Override
    public String getIconClassName() {
      return "icon-scm-manager-navigator";
    }

    @Override
    public ScmManagerNavigator newInstance(String projectName) {
      ScmManagerNavigator scmManagerNavigator = new ScmManagerNavigator(projectName);
      scmManagerNavigator.setTraits(getTraitsDefaults());
      return scmManagerNavigator;
    }

    @SuppressWarnings("unused") // used By stapler
    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value) {
      return ConnectionConfiguration.fillCredentialsIdItems(context, serverUrl, value);
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
      if (Strings.isNullOrEmpty(serverUrl) || Strings.isNullOrEmpty(credentialsId)) {
        return createEmptyNamespaceSelect(value);
      }

      ScmManagerApi api;
      try {
        api = apiFactory.create(context, serverUrl, credentialsId);
      } catch (CredentialsUnavailableException e) {
        return createEmptyNamespaceSelect(value);
      }
      ListBoxModel model = new ListBoxModel();
      model.add(ALL_NAMESPACES_LABEL);
      api
        .getNamespaces()
        .exceptionally(e -> emptyList())
        .get()
        .stream()
        .map(Namespace::getNamespace)
        .sorted()
        .forEach(n -> model.add(n));
      return model;
    }

    private ListBoxModel createEmptyNamespaceSelect(String value) {
      ListBoxModel model = new ListBoxModel();
      if (!Strings.isNullOrEmpty(value)) {
        model.add(value);
      }
      return model;
    }

    @SuppressWarnings({"unused", "rawtypes"}) // used By stapler, generic hell
    public List<NamedArrayList<? extends SCMTraitDescriptor>> getTraitsDescriptorLists() {
      List<NamedArrayList<? extends SCMTraitDescriptor>> all = new ArrayList<>(delegate.getTraitsDescriptorLists());
      if (Subversion.isSupported()) {
        appendSubVersionTrait(all);
      }
      return all;
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // generic hell
    private void appendSubVersionTrait(List<NamedArrayList<? extends SCMTraitDescriptor>> all) {
      Optional<SCMNavigatorTraitDescriptor> descriptor = SCMNavigatorTrait._for(null, ScmManagerNavigatorContext.class, null)
        .stream().filter(desc -> desc instanceof ScmManagerSvnNavigatorTrait.DescriptorImpl).findFirst();
      if (descriptor.isPresent()) {
        for (NamedArrayList list : all) {
          if ("Within repository".equals(list.getName())) {
            list.add(descriptor.get());
          }
        }
      }
    }

    @NonNull
    @Override
    public List<SCMTrait<? extends SCMTrait<?>>> getTraitsDefaults() {
      List<SCMTrait<? extends SCMTrait<?>>> traits = new ArrayList<>(delegate.getTraitsDefaults());
      if (Subversion.isSupported()) {
        traits.add(new ScmManagerSvnNavigatorTrait());
      }
      return traits;
    }

    @NonNull
    @Override
    protected SCMSourceCategory[] createCategories() {
      return new SCMSourceCategory[]{
        new UncategorizedSCMSourceCategory(new NonLocalizable("Repositories"))
      };
    }
  }

  public static class NavigatorWitness implements SCMNavigatorRequest.Witness {

    private final TaskListener listener;

    public NavigatorWitness(TaskListener listener) {
      this.listener = listener;
    }

    @Override
    public void record(@NonNull String projectName, boolean isMatch) {
      if (isMatch) {
        listener.getLogger().format("Proposing %s%n", projectName);
      } else {
        listener.getLogger().format("Ignoring %s%n", projectName);
      }
    }
  }
}

