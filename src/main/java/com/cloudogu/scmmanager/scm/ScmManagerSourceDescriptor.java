package com.cloudogu.scmmanager.scm;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.scm.api.Authentications;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import de.otto.edison.hal.HalRepresentation;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceOwner;
import org.acegisecurity.Authentication;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

public class ScmManagerSourceDescriptor extends SCMSourceDescriptor {

  protected final BiFunction<String, HttpAuthentication, ScmManagerApi> apiFactory;
  private final Predicate<Repository> repositoryPredicate;

  @VisibleForTesting
  ScmManagerSourceDescriptor(BiFunction<String, HttpAuthentication, ScmManagerApi> apiFactory, Predicate<Repository> repositoryPredicate) {
    this.apiFactory = apiFactory;
    this.repositoryPredicate = repositoryPredicate;
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
        ListBoxModel.Option option = createRepositoryOption(repository);
        if (option != null) {
          model.add(option);
        }
      }
    }
    return model;
  }

  protected ListBoxModel.Option createRepositoryOption(Repository repository) {
    String displayName = String.format("%s/%s (%s)", repository.getNamespace(), repository.getName(), repository.getType());
    String v = String.format("%s/%s/%s", repository.getNamespace(), repository.getName(), repository.getType());
    return new ListBoxModel.Option(displayName, v);
  }

  static {
    Icons.register("icon-scm-manager-source");
  }

  @Override
  public String getIconClassName() {
    return "icon-scm-manager-source";
  }

}
