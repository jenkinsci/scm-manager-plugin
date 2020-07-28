package com.cloudogu.scmmanager.scm;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.scm.api.ApiClient;
import com.cloudogu.scmmanager.scm.api.Authentications;
import com.cloudogu.scmmanager.scm.api.Branch;
import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.PullRequest;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerObservable;
import com.cloudogu.scmmanager.scm.api.Tag;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import de.otto.edison.hal.HalRepresentation;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceOwner;
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
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Collections.emptyList;

public class ScmManagerSource extends SCMSource {

  private final String serverUrl;
  private final String namespace;
  private final String name;
  private final String type;
  private final String credentialsId;

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

      // this is fetch all
      // TODO handle includes from criteria

      observe(observer, request);
    }
  }

  @VisibleForTesting
  void observe(@NonNull SCMHeadObserver observer, ScmManagerSourceRequest request) throws IOException, InterruptedException {
    HttpAuthentication authentication = getAuthenticationsProvider().apply(getOwner()).from(serverUrl, credentialsId);

    ScmManagerApi api = getApiFactory().apply(serverUrl, authentication);
    Repository repository = null;
    try {
      repository = api.getRepository(namespace, name).get();
    } catch (ExecutionException e) {
      ApiClient.handleException(e);
      throw new UncheckedIOException(new IOException("failed to load repository"));
    }

    // TODO evaluate event and check only what's necessary

    CompletableFuture<List<Branch>> branchesFuture = request.isFetchBranches() ? api.getBranches(repository) : CompletableFuture.completedFuture(Collections.emptyList());
    CompletableFuture<List<Tag>> tagsFuture = request.isFetchTags() ? api.getTags(repository) : CompletableFuture.completedFuture(Collections.emptyList());
    CompletableFuture<List<PullRequest>> pullRequestFuture = request.isFetchPullRequests() ? api.getPullRequests(repository) : CompletableFuture.completedFuture(Collections.emptyList());

    CompletableFuture.allOf(
      branchesFuture,
      tagsFuture,
      pullRequestFuture
    ).join();

    try {
      observe(observer, branchesFuture.get());
      observe(observer, tagsFuture.get());
      observe(observer, pullRequestFuture.get());
    } catch (ExecutionException e) {
      ApiClient.handleException(e);
      // TODO error handling
    }
  }

  private void observe(SCMHeadObserver observer, List<? extends ScmManagerObservable> observables) throws IOException, InterruptedException {
    for (ScmManagerObservable observable : observables) {
      observer.observe(observable.head(), observable.revision());
    }
  }

  @NonNull
  @Override
  public SCM build(@NonNull SCMHead head, SCMRevision revision) {
    if (head instanceof ScmManagerHead) {
      ScmManagerHead scmHead = (ScmManagerHead) head;
      CloneInformation cloneInformation = (scmHead).getCloneInformation();
      if (cloneInformation.getType().equals("git")) {
        return new ScmManagerGitSCMBuilder(scmHead, revision, cloneInformation.getUrl(), credentialsId).build();
      }
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

  @Extension
  @Symbol("scm-manager")
  public static class DescriptorImpl extends SCMSourceDescriptor {

    private final BiFunction<String, HttpAuthentication, ScmManagerApi> apiFactory;

    public DescriptorImpl() {
      this(ScmManagerSource::createHttpClient);
    }

    public DescriptorImpl(BiFunction<String, HttpAuthentication, ScmManagerApi> apiFactory) {
      this.apiFactory = apiFactory;
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
        String displayName = String.format("%s/%s (%s)", repository.getNamespace(), repository.getName(), repository.getType());
        String v = String.format("%s/%s/%s", repository.getNamespace(), repository.getName(), repository.getType());
        model.add(displayName, v);
      }
      return model;
    }

    // need to implement this as the default filtering of form binding will not be specific enough
    public List<SCMSourceTraitDescriptor> getTraitsDescriptors() {
      // Git builder ?? what about hg and svn?
      return SCMSourceTrait._for(this, ScmManagerSourceContext.class, ScmManagerGitSCMBuilder.class);
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
  }

  private static ScmManagerApi createHttpClient(String value, HttpAuthentication authentication) {
    return new ScmManagerApi(new ApiClient(value, authentication));
  }

}
