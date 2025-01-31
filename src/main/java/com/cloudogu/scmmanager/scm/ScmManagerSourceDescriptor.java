package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.NamespaceAndName;
import com.cloudogu.scmmanager.scm.api.Namespace;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import hudson.model.AutoCompletionCandidates;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceOwner;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

public class ScmManagerSourceDescriptor extends SCMSourceDescriptor {

  @VisibleForTesting
  ScmManagerSourceDescriptor(ScmManagerApiFactory apiFactory, Predicate<Repository> repositoryPredicate) {
    this.apiFactory = apiFactory;
    this.repositoryPredicate = repositoryPredicate;
  }

  /**
   * <b>Warning:</b> This extended constructor is intended for testing purposes only.
   * @param apiFactory - Api factory
   * @param repositoryPredicate - Repository predicate
   * @param serverUrl - ServerUrl. Note that this is expected to be inserted by a Jenkins form in production!
   * @param credentialsId - Id of the Jenkins credential object. Note that this is expected to be inserted by a Jenkins form in production!
   */
  @VisibleForTesting
  ScmManagerSourceDescriptor(ScmManagerApiFactory apiFactory, Predicate<Repository> repositoryPredicate, String serverUrl, String credentialsId) {
    this(apiFactory, repositoryPredicate);
    this.serverUrl = serverUrl;
    this.credentialsId = credentialsId;
  }
  static final int AUTO_COMPLETION_MIN = 2;
  static final int TIME_OUT_IN_MILLIS = 3500;
  static final int MAX_RESULTS = 5;

  protected final ScmManagerApiFactory apiFactory;
  private final Predicate<Repository> repositoryPredicate;

  private String serverUrl;
  private String credentialsId;
  private AutoCompletionCandidates cachedCandidates = new AutoCompletionCandidates();

  private static final Logger LOG = LoggerFactory.getLogger(ScmManagerSourceDescriptor.class);


  @SuppressWarnings("unused") // used By stapler
  public FormValidation doCheckServerUrl(@QueryParameter String value) throws InterruptedException, ExecutionException {
    FormValidation validation = ConnectionConfiguration.checkServerUrl(apiFactory, value);
    if(validation.equals(FormValidation.ok())) {
      this.serverUrl = value;
    }
    return validation;
  }

  @SuppressWarnings("unused") // used By stapler
  public FormValidation doCheckCredentialsId(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value) throws InterruptedException, ExecutionException {
    FormValidation validation = validateCredentialsId(context, serverUrl, value);
    if(validation.equals(FormValidation.ok())) {
      this.credentialsId = value;
    }
    return validation;
  }

  @VisibleForTesting
  FormValidation validateCredentialsId(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value) throws InterruptedException, ExecutionException {
    return ConnectionConfiguration.validateCredentialsId(apiFactory, context, serverUrl, value);
  }

  @SuppressWarnings("unused") // used By stapler
  public ListBoxModel doFillCredentialsIdItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value) {
    return ConnectionConfiguration.fillCredentialsIdItems(context, serverUrl, value);
  }

  @SuppressWarnings("unused") // used by Stapler
  public FormValidation doCheckRepository(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String credentialsId, @QueryParameter String value) {
    if(value.length() < AUTO_COMPLETION_MIN) {
      return FormValidation.warning(String.format("Please enter at least %d characters and a namespace for suggestions.", AUTO_COMPLETION_MIN));
    }
    boolean isEmpty = this.cachedCandidates.getValues().isEmpty()
      || cachedCandidates.getValues().stream().filter(c -> c.equals(value)).findFirst().isEmpty();
    if(isEmpty) {
      return FormValidation.error("Repository not found");
    } else {
      return FormValidation.ok();
    }
  }

  @SuppressWarnings("unused") // used by Stapler
  public AutoCompletionCandidates doAutoCompleteRepository(@AncestorInPath SCMSourceOwner context, @QueryParameter String value) throws ExecutionException, InterruptedException {
      return autoCompleteRepository(context, serverUrl, credentialsId, value);
  }

  public AutoCompletionCandidates autoCompleteRepository(@AncestorInPath SCMSourceOwner context, String serverUrl, String credentialsId, @QueryParameter String value) throws InterruptedException, ExecutionException {
    if (Strings.isNullOrEmpty(serverUrl) || Strings.isNullOrEmpty(credentialsId)) {
      LOG.debug("ServerUrl or CredentialsId were empty or null, so no autocomplete suggestions returned.");
      return new AutoCompletionCandidates();
    }
    if (value == null) {
      value = "";
    }

    ScmManagerApi api = apiFactory.create(context, serverUrl, credentialsId);

    String version = api.index().get().getVersion();

    // filter all repositories that do not support the protocol
    if(isLegacyAutoCompleteVersion(version)) {
      AutoCompletionCandidates candidates = autoCompleteRepositoryWithSingleNamespaceScope(api, value);
      cachedCandidates = candidates;
      return candidates;
    } else {
      AutoCompletionCandidates candidates = autoCompleteRepository(api, value);
      cachedCandidates = candidates;
      return candidates;
    }
  }

  /**
   * Versions below SCM-Manager 3.7.2 do not support queries in <tt>namespace/name</tt>
   * format and need a different AutoComplete approach.
   * @param version SCM-Manager version
   */
  private boolean isLegacyAutoCompleteVersion(String version) {
    String[] parts = version.split("\\.");
    int major = Integer.parseInt(parts[0]);
    int minor = Integer.parseInt(parts[1]);
    int patch = Integer.parseInt(parts[2]);

    return major < 3 || minor < 7 || patch < 2;
  }



  private AutoCompletionCandidates autoCompleteRepository(ScmManagerApi api, String value) throws ExecutionException, InterruptedException {
    return autoCompleteCandidates(api, new ScmManagerApi.SearchQuery(value, null));
  }

  /**
   * In this mode, only values within an already-entered namespace scope can be searched for.
   *
   * This is the standard (legacy) mode up to SCM-Manager 3.7.1.
   *
   * @param api Api
   * @param value Value in the textbox
   * @return AutoCompletion candidates
   */
  private AutoCompletionCandidates autoCompleteRepositoryWithSingleNamespaceScope(ScmManagerApi api, String value) throws ExecutionException, InterruptedException {
   NamespaceAndName extracted = extractNamespaceAndName(value);

    Optional<Namespace> namespace = api.getNamespaces().exceptionally(e -> emptyList()).get().stream()
      .filter(n -> n.getNamespace().equals(extracted.getNamespace())).findFirst();

    if(namespace.isPresent()) {
      return autoCompleteCandidates(api, new ScmManagerApi.SearchQuery(extracted.getName(), extracted.getNamespace()));
    } else {
      return new AutoCompletionCandidates();
    }
  }

  private NamespaceAndName extractNamespaceAndName(String combinedValue) {
    String namespaceString = combinedValue.split("/")[0];
    String repositoryString = null;
    if(combinedValue.split("/").length > 1) {
      repositoryString = combinedValue.split("/")[1];
    }
    return new NamespaceAndName(namespaceString, repositoryString);
  }

  private AutoCompletionCandidates autoCompleteCandidates(ScmManagerApi api, ScmManagerApi.SearchQuery query) throws ExecutionException, InterruptedException {
    AutoCompletionCandidates candidates = new AutoCompletionCandidates();

    Predicate<Repository> protocolPredicate = repository -> repository.getUrl(api.getProtocol()).isPresent();
    Predicate<Repository> predicate = protocolPredicate.and(repositoryPredicate);

    try {
      List<Repository> repositories = api.getRepositories(query).exceptionally(e -> emptyList()).get(TIME_OUT_IN_MILLIS, TimeUnit.MILLISECONDS);
      for (Repository repository : repositories) {
        if (candidates.getValues().size() < MAX_RESULTS && predicate.test(repository)) {
          String option = createRepositoryOption(repository);
          if (option != null) {
            candidates.add(option);
          }
        }
      }
      return candidates;
    } catch(TimeoutException e) {
      throw new ExecutionException("Repository loading failed due to a timeout or load issue by the SCM-Manager instance.", e);
    }
  }

  protected String createRepositoryOption(Repository repository) {
    return String.format("%s/%s", repository.getNamespace(), repository.getName(), repository.getType());
  }

  static {
    Icons.register("icon-scm-manager-source");
  }

  @Override
  public String getIconClassName() {
    return "icon-scm-manager-source";
  }

}
