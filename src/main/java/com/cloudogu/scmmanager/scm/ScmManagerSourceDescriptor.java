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

  static final String NOT_CONTAIN_A_SLASH = "Repository name must not contain a slash.";

  ScmManagerSourceDescriptor(ScmManagerApiFactory apiFactory, Predicate<Repository> repositoryPredicate) {
    this.apiFactory = apiFactory;
    this.repositoryPredicate = repositoryPredicate;
    this.configuration = new Configuration(Configuration.DEFAULT_TIME_OUT_IN_MILLIS, Configuration.DEFAULT_MAX_RESULTS);
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

  public record Configuration(int timeoutInMillis, int maxResults) {
    // TODO Should be reduced once performance fix has been done in SCMM.
    static final int DEFAULT_TIME_OUT_IN_MILLIS = 120000;
    static final int DEFAULT_MAX_RESULTS = 5;
  }

  protected final ScmManagerApiFactory apiFactory;
  private final Predicate<Repository> repositoryPredicate;

  private Configuration configuration;

  @VisibleForTesting
  void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  private String serverUrl;
  private String credentialsId;
  private AutoCompletionCandidates returnedCandidates = new AutoCompletionCandidates();

  private AutoCompletionFormMessage autocompletionFormMessage = null;
  private record AutoCompletionFormMessage(String message, FormValidation.Kind kind) {}

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
    if(autocompletionFormMessage != null) {
      return switch (autocompletionFormMessage.kind) {
        case ERROR -> FormValidation.error(autocompletionFormMessage.message);
        case WARNING -> FormValidation.warning(autocompletionFormMessage.message);
        case OK -> FormValidation.ok(autocompletionFormMessage.message);
      };
    }
    boolean isEmpty = this.returnedCandidates.getValues().isEmpty()
      || returnedCandidates.getValues().stream().filter(c -> c.equals(value)).findFirst().isEmpty();
    if(isEmpty) {
      return FormValidation.warning("No repositories found.");
    } else {
      return FormValidation.ok();
    }
  }

  @SuppressWarnings("unused") // used by Stapler
  public AutoCompletionCandidates doAutoCompleteRepository(@AncestorInPath SCMSourceOwner context, @QueryParameter String value) throws InterruptedException {
    try {
      return autoCompleteRepository(context, serverUrl, credentialsId, value);
    } catch (ExecutionException e) {
      this.autocompletionFormMessage = new AutoCompletionFormMessage(e.getMessage(), FormValidation.Kind.ERROR);
      return new AutoCompletionCandidates();
    }
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
      returnedCandidates = candidates;
      return candidates;
    } else {
      AutoCompletionCandidates candidates = autoCompleteRepository(api, value);
      returnedCandidates = candidates;
      return candidates;
    }
  }

  /**
   * Versions below SCM-Manager 3.7.2 do not support queries in <tt>namespace/name</tt>
   * format and need a different AutoComplete approach.
   * @param version SCM-Manager version
   */
  private boolean isLegacyAutoCompleteVersion(String version) {
    try {
      String[] parts = version.split("\\.");
      int major = Integer.parseInt(parts[0]);
      int minor = Integer.parseInt(parts[1]);
      int patch = Integer.parseInt(parts[2]);

      return major < 3 || minor < 7 || patch < 2;
    } catch (NumberFormatException e) {
      return false;
    }
  }



  private AutoCompletionCandidates autoCompleteRepository(ScmManagerApi api, String value) throws ExecutionException, InterruptedException {
    return autoCompleteCandidates(api, new ScmManagerApi.RepositorySearchQuery(value, null));
  }

  /**
   * In this mode, only values within an already-entered namespace scope can be searched for.
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
      return autoCompleteCandidates(api, new ScmManagerApi.RepositorySearchQuery(extracted.getName(), extracted.getNamespace()));
    } else {
      autocompletionFormMessage = new AutoCompletionFormMessage(
        String.format("Namespace '%s' is not available. Namespace search is supported with SCM-Manager 3.7.2+.", extracted.getNamespace()),
        FormValidation.Kind.WARNING);
      return new AutoCompletionCandidates();
    }
  }

  private NamespaceAndName extractNamespaceAndName(String combinedValue) throws ExecutionException {
    String repositoryString = null;
    String[] split = combinedValue.split("/");
    String namespaceString = split[0];
    if(split.length > 2) {
      autocompletionFormMessage = new AutoCompletionFormMessage(NOT_CONTAIN_A_SLASH, FormValidation.Kind.ERROR);
      throw new ExecutionException(new Exception(NOT_CONTAIN_A_SLASH));
    } else if(combinedValue.endsWith("/")) {
      repositoryString = "";
    }

    return new NamespaceAndName(namespaceString, repositoryString);
  }

  private AutoCompletionCandidates autoCompleteCandidates(ScmManagerApi api, ScmManagerApi.RepositorySearchQuery query) throws ExecutionException, InterruptedException {
    AutoCompletionCandidates candidates = new AutoCompletionCandidates();

    if(query.toString().split("/").length > 2) {
      autocompletionFormMessage = new AutoCompletionFormMessage(NOT_CONTAIN_A_SLASH, FormValidation.Kind.ERROR);
      throw new ExecutionException(new Exception(NOT_CONTAIN_A_SLASH));
    }

    Predicate<Repository> protocolPredicate = repository -> repository.getUrl(api.getProtocol()).isPresent();
    Predicate<Repository> predicate = protocolPredicate.and(repositoryPredicate);

    try {
      List<Repository> repositories = api.getRepositories(query).exceptionally(
        e -> emptyList()).get(configuration.timeoutInMillis(), TimeUnit.MILLISECONDS);
      for (Repository repository : repositories) {
        if (candidates.getValues().size() < configuration.maxResults() && predicate.test(repository)) {
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
    return String.format("%s/%s", repository.getNamespace(), repository.getName());
  }

  static {
    Icons.register("icon-scm-manager-source");
  }

  @Override
  public String getIconClassName() {
    return "icon-scm-manager-source";
  }

}
