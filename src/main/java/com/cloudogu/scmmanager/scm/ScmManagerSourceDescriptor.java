package com.cloudogu.scmmanager.scm;

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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

public class ScmManagerSourceDescriptor extends SCMSourceDescriptor {

  protected final ScmManagerApiFactory apiFactory;
  private final Predicate<Repository> repositoryPredicate;
  private String serverUrl;
  private String credentialsId;

  @VisibleForTesting
  ScmManagerSourceDescriptor(ScmManagerApiFactory apiFactory, Predicate<Repository> repositoryPredicate) {
    this.apiFactory = apiFactory;
    this.repositoryPredicate = repositoryPredicate;
  }

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

  /*
  @SuppressWarnings("unused") // used By stapler
  public ComboBoxModel doFillRepositoryItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String credentialsId, @QueryParameter String value) throws InterruptedException, ExecutionException {
    return fillRepositoryItems(context, serverUrl, credentialsId, value);
  }
  */

  @SuppressWarnings("unused") // used by Stapler
  public FormValidation doCheckRepository(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String credentialsId, @QueryParameter String value) throws ExecutionException, InterruptedException {
    return FormValidation.ok();
  }

  @SuppressWarnings("unused") // used by Stapler
  public AutoCompletionCandidates doAutoCompleteRepository(@AncestorInPath SCMSourceOwner context, @QueryParameter String value) throws ExecutionException, InterruptedException {
      return autoCompleteRepository(context, serverUrl, credentialsId, value);
  }

  public AutoCompletionCandidates autoCompleteRepository(@AncestorInPath SCMSourceOwner context, String serverUrl, String credentialsId, @QueryParameter String value) throws InterruptedException, ExecutionException {
    AutoCompletionCandidates candidates = new AutoCompletionCandidates();
    //TODO REMOVE
    System.out.println("Value: " + value);
    if (Strings.isNullOrEmpty(serverUrl) || Strings.isNullOrEmpty(credentialsId)) {
      return candidates;
    }

    ScmManagerApi api = apiFactory.create(context, serverUrl, credentialsId);

    // filter all repositories that do not support the protocol
    Predicate<Repository> protocolPredicate = repository -> repository.getUrl(api.getProtocol()).isPresent();
    Predicate<Repository> predicate = protocolPredicate.and(repositoryPredicate);
    List<Repository> repositories = api.getRepositories().exceptionally(e -> emptyList()).get();
    for (Repository repository : repositories) {
      if (predicate.test(repository)) {
        String option = createRepositoryOption(repository);
        if (option != null) {
          candidates.add(option);
        }
      }
    }
    return candidates;
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
