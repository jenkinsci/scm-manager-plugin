package com.cloudogu.scmmanager.scm;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.scm.api.ApiClient;
import com.cloudogu.scmmanager.scm.api.Authentications;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerObservable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import de.otto.edison.hal.HalRepresentation;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMEvent;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMProbe;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceEvent;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.TagSCMHeadCategory;
import jenkins.scm.impl.UncategorizedSCMHeadCategory;
import jenkins.util.NonLocalizable;
import org.acegisecurity.Authentication;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

public class ScmManagerSource extends SCMSource {

  private final String serverUrl;
  private final String namespace;
  private final String name;
  private final String type;
  private final String credentialsId;

  private final LinkBuilder linkBuilder;

  @NonNull
  private List<SCMSourceTrait> traits = new ArrayList<>();

  private BiFunction<String, HttpAuthentication, ScmManagerApi> apiFactory;
  private Function<SCMSourceOwner, Authentications> authenticationsProvider;

  @DataBoundConstructor
  public ScmManagerSource(String serverUrl, String repository, String credentialsId) {
    this(serverUrl, repository, credentialsId, ScmManagerSource::createHttpClient, Authentications::new);
  }

  ScmManagerSource(
    String serverUrl,
    String repository,
    String credentialsId,
    BiFunction<String, HttpAuthentication, ScmManagerApi> apiFactory,
    Function<SCMSourceOwner, Authentications> authenticationsProvider
  ) {
    this.serverUrl = serverUrl;
    this.credentialsId = credentialsId;

    String[] parts = repository.split("/");
    this.namespace = parts[0];
    this.name = parts[1];
    this.type = parts[2];

    this.apiFactory = apiFactory;
    this.authenticationsProvider = authenticationsProvider;

    this.linkBuilder = new LinkBuilder(serverUrl, namespace, name);
  }

  @NonNull
  @Override
  public List<SCMSourceTrait> getTraits() {
    return Collections.unmodifiableList(traits);
  }

  @Override
  @DataBoundSetter
  public void setTraits(@CheckForNull List<SCMSourceTrait> traits) {
    this.traits = new ArrayList<>(Util.fixNull(traits));
  }

  String getNamespace() {
    return namespace;
  }

  String getName() {
    return name;
  }

  String getType() {
    return type;
  }

  @Override
  protected void retrieve(SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer, SCMHeadEvent<?> event, @NonNull TaskListener listener) throws IOException, InterruptedException {
    try (ScmManagerSourceRequest request = new ScmManagerSourceContext(criteria, observer)
      .withTraits(traits)
      .newRequest(this, listener)) {
      handleRequest(observer, event, request);
    }
  }

  @VisibleForTesting
  void handleRequest(@NonNull SCMHeadObserver observer, SCMHeadEvent<?> event, ScmManagerSourceRequest request) throws InterruptedException, IOException {
    Iterable<ScmManagerObservable> candidates = null;

    ScmManagerSourceRetriever handler = ScmManagerSourceRetriever.create(
      createApi(),
      namespace,
      name
    );

    // for now we trigger a full scan for deletions
    // TODO improve handling of deletions
    if (event == null || event.getType() != SCMEvent.Type.REMOVED) {
      Set<SCMHead> includes = observer.getIncludes();
      if (includes != null && includes.size() == 1) {
        candidates = handler.getSpecificCandidatesFromSourceControl(request, includes.iterator().next());
      }
    }

    if (candidates == null) {
      candidates = handler.getAllCandidatesFromSourceControl(request);
    }
    for (ScmManagerObservable candidate : candidates) {
      if (request.process(candidate.head(), candidate.revision(), handler::probe, new CriteriaWitness(request))) {
        return;
      }
    }
  }

  @NonNull
  @Override
  protected SCMProbe createProbe(@NonNull SCMHead head, @CheckForNull SCMRevision revision) {
    ScmManagerSourceRetriever handler = ScmManagerSourceRetriever.create(createApi(), namespace, name);
    return handler.probe(head, revision);
  }

  private ScmManagerApi createApi() {
    HttpAuthentication authentication = getAuthenticationsProvider().apply(getOwner()).from(serverUrl, credentialsId);
    return getApiFactory().apply(serverUrl, authentication);
  }

  @NonNull
  @Override
  public SCM build(@NonNull SCMHead head, SCMRevision revision) {
    if (head instanceof ScmManagerHead) {
      SCMBuilderProvider.Context ctx = new SCMBuilderProvider.Context(
        linkBuilder,
        (ScmManagerHead) head,
        revision,
        credentialsId
      );
      return SCMBuilderProvider.from(ctx).build();
    }
    throw new IllegalArgumentException("Could not handle unknown SCMHead: " + head);
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public String getRepository() {
    return String.format("%s/%s/%s", namespace, name, type);
  }

  public String getCredentialsId() {
    return credentialsId;
  }

  private BiFunction<String, HttpAuthentication, ScmManagerApi> getApiFactory() {
    if (apiFactory == null) {
      apiFactory = ScmManagerSource::createHttpClient;
    }
    return apiFactory;
  }

  private Function<SCMSourceOwner, Authentications> getAuthenticationsProvider() {
    if (authenticationsProvider == null) {
      authenticationsProvider = Authentications::new;
    }
    return authenticationsProvider;
  }

  private static ScmManagerApi createHttpClient(String value, HttpAuthentication authentication) {
    return new ScmManagerApi(new ApiClient(value, authentication));
  }

  static {
    Icons.register("icon-scm-manager-link");
  }

  @NonNull
  @Override
  protected List<Action> retrieveActions(@NonNull SCMRevision revision, SCMHeadEvent event, @NonNull TaskListener listener) {
    return Collections.singletonList(
      new ScmManagerLink("icon-scm-manager-link", linkBuilder.create(revision))
    );
  }

  @NonNull
  @Override
  protected List<Action> retrieveActions(@NonNull SCMHead head, SCMHeadEvent event, @NonNull TaskListener listener) {
    return Collections.singletonList(
      new ScmManagerLink("icon-scm-manager-link", linkBuilder.create(head))
    );
  }

  @NonNull
  @Override
  protected List<Action> retrieveActions(@CheckForNull SCMSourceEvent event, @NonNull TaskListener listener) {
    return Collections.singletonList(
      new ScmManagerLink("icon-scm-manager-link", linkBuilder.repo())
    );
  }

  @Extension
  @Symbol("scm-manager")
  public static class DescriptorImpl extends SCMSourceDescriptor {

    private final BiFunction<String, HttpAuthentication, ScmManagerApi> apiFactory;

    private final Predicate<Repository> repositoryPredicate;

    public DescriptorImpl() {
      this(ScmManagerSource::createHttpClient, SCMBuilderProvider::isSupported);
    }

    public DescriptorImpl(BiFunction<String, HttpAuthentication, ScmManagerApi> apiFactory, Predicate<Repository> repositoryPredicate) {
      this.apiFactory = apiFactory;
      this.repositoryPredicate = repositoryPredicate;
    }

    @Nonnull
    @Override
    public String getDisplayName() {
      return "SCM-Manager";
    }

    @SuppressWarnings("unused") // used By stapler
    public FormValidation doCheckServerUrl(@QueryParameter String value) throws InterruptedException, ExecutionException {
      String trimmedValue = value.trim();
      if (Strings.isNullOrEmpty(trimmedValue)) {
        return FormValidation.error("server url is required");
      }
      try {
        URI uri = new URI(value);
        if (!uri.isAbsolute()) {
          return FormValidation.error("illegal URL format");
        }
        if (!uri.getScheme().startsWith("http")) {
          return FormValidation.error("Only http or https urls accepted");
        }
      } catch (URISyntaxException e) {
        return FormValidation.error("illegal URL format");
      }


      ScmManagerApi api = apiFactory.apply(value, x -> {
      });
      CompletableFuture<HalRepresentation> future = api.index();
      return future
        .thenApply(index -> {
          if (index.getLinks().getLinkBy("login").isPresent()) {
            return FormValidation.ok();
          }
          return FormValidation.error("api has no login link");
        })
        .exceptionally(e -> FormValidation.error(e.getMessage()))
        .get();
    }

    @SuppressWarnings("unused") // used By stapler
    public FormValidation doCheckCredentialsId(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value) throws InterruptedException, ExecutionException {
      return validateCredentialsId(context, serverUrl, value, Authentications::new);
    }

    @VisibleForTesting
    FormValidation validateCredentialsId(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value, Function<SCMSourceOwner, Authentications> authenticationsProvider) throws InterruptedException, ExecutionException {
      if (doCheckServerUrl(serverUrl).kind != FormValidation.Kind.OK) {
        return FormValidation.error("server url is required");
      }
      if (Strings.isNullOrEmpty(value)) {
        return FormValidation.error("credentials are required");
      }
      Authentications authentications = authenticationsProvider.apply(context);
      ScmManagerApi client = apiFactory.apply(serverUrl, authentications.from(serverUrl, value));
      CompletableFuture<HalRepresentation> future = client.index();
      return future
        .thenApply(index -> {
          if (index.getLinks().getLinkBy("me").isPresent()) {
            return FormValidation.ok();
          }
          return FormValidation.error("login failed");
        })
        .exceptionally(e -> FormValidation.error(e.getMessage()))
        .get();
    }

    @SuppressWarnings("unused") // used By stapler
    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String value) {
      if (context == null || !context.hasPermission(Item.CONFIGURE)) {
        return new StandardUsernameListBoxModel().includeCurrentValue(value);
      }
      Authentication authentication = context instanceof Queue.Task
        ? ((Queue.Task) context).getDefaultAuthentication()
        : ACL.SYSTEM;
      return new StandardUsernameListBoxModel()
        .includeEmptyValue()
        .includeAs(authentication, context, StandardUsernameCredentials.class, URIRequirementBuilder.fromUri(value).build())
        .includeCurrentValue(value);
    }

    @SuppressWarnings("unused") // used By stapler
    public ListBoxModel doFillRepositoryItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String credentialsId, @QueryParameter String value) throws InterruptedException, ExecutionException {
      return fillRepositoryItems(context, serverUrl, credentialsId, value, Authentications::new);
    }

    public ListBoxModel fillRepositoryItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String credentialsId, @QueryParameter String value, Function<SCMSourceOwner, Authentications> authenticationsProvider) throws InterruptedException, ExecutionException {
      ListBoxModel model = new ListBoxModel();
      if (Strings.isNullOrEmpty(serverUrl) || Strings.isNullOrEmpty(credentialsId)) {
        if (!Strings.isNullOrEmpty(value)) {
          model.add(value);
        }
        return model;
      }

      Authentications authentications = authenticationsProvider.apply(context);
      ScmManagerApi api = apiFactory.apply(serverUrl, authentications.from(serverUrl, credentialsId));
      List<Repository> repositories = api.getRepositories().exceptionally(e -> emptyList()).get();
      for (Repository repository : repositories) {
        if (repositoryPredicate.test(repository)) {
          String displayName = String.format("%s/%s (%s)", repository.getNamespace(), repository.getName(), repository.getType());
          String v = String.format("%s/%s/%s", repository.getNamespace(), repository.getName(), repository.getType());
          model.add(displayName, v);
        }
      }
      return model;
    }

    @SuppressWarnings("unused") // used By stapler
    public List<SCMSourceTraitDescriptor> getTraitsDescriptors() {
      // we use a LinkedHashSet to deduplicate and keep order
      Set<SCMSourceTraitDescriptor> traitDescriptors = new LinkedHashSet<>();
      traitDescriptors.addAll(SCMSourceTrait._for(this, ScmManagerSourceContext.class, null));
      for (SCMBuilderProvider provider : SCMBuilderProvider.all()) {
        traitDescriptors.addAll(provider.getTraitDescriptors(this));
      }
      return new ArrayList<>(traitDescriptors);
    }

    @Override
    @NonNull
    public List<SCMSourceTrait> getTraitsDefaults() {
      return Arrays.asList(
        new BranchDiscoveryTrait(),
        new PullRequestDiscoveryTrait()
      );
    }
    @NonNull
    @Override
    protected SCMHeadCategory[] createCategories() {
      return new SCMHeadCategory[]{
        UncategorizedSCMHeadCategory.DEFAULT,
        // TODO do we have to localize it
        new ChangeRequestSCMHeadCategory(new NonLocalizable("Pull Requests")),
        TagSCMHeadCategory.DEFAULT
      };
    }

    static {
      Icons.register("icon-scm-manager-source");
    }

    @Override
    public String getIconClassName() {
      return "icon-scm-manager-source";
    }

  }

  private static class CriteriaWitness implements SCMSourceRequest.Witness {

    private final ScmManagerSourceRequest request;

    public CriteriaWitness(ScmManagerSourceRequest request) {
      this.request = request;
    }
    @Override
    public void record(@NonNull SCMHead scmHead, SCMRevision revision, boolean isMatch) {
      PrintStream logger = request.listener().getLogger();
      logger.append("    ").append(scmHead.getName()).append(": ");
      if (revision == null) {
        logger.println("Skipped");
      } else {
        if (isMatch) {
          logger.println("Met criteria");
        } else {
          logger.println("Does not meet criteria");
        }
      }
    }

  }

}
