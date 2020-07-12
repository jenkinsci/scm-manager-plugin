package com.cloudogu.scmmanager.scm;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.scm.api.ApiClient;
import com.cloudogu.scmmanager.scm.api.ApiClient.Promise;
import com.cloudogu.scmmanager.scm.api.Authentications;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import de.otto.edison.hal.HalRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceOwner;
import org.acegisecurity.Authentication;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Collections.emptyList;

public class ScmManagerSource extends SCMSource {

  private final String serverUrl;
  private final String namespace;
  private final String name;
  private final String credentialsId;

  @DataBoundConstructor
  public ScmManagerSource(String serverUrl, String repository, String credentialsId) {
    this.serverUrl = serverUrl;
    this.credentialsId = credentialsId;

    String[] parts = repository.split("/");
    this.namespace = parts[0];
    this.name = parts[1];
  }

  @Override
  protected void retrieve(SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer, SCMHeadEvent<?> event, @NonNull TaskListener listener) throws IOException, InterruptedException {

  }

  @NonNull
  @Override
  public SCM build(@NonNull SCMHead head, SCMRevision revision) {
    return null;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  @Extension
  @Symbol("scm-manager")
  public static class DescriptorImpl extends SCMSourceDescriptor {

    private final BiFunction<String, HttpAuthentication, ScmManagerApi> apiFactory;

    public DescriptorImpl() {
      this(DescriptorImpl::createHttpClient);
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
    public FormValidation doCheckServerUrl(@QueryParameter String value) throws InterruptedException {
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


      ScmManagerApi api = apiFactory.apply(value, x -> {});
      Promise<HalRepresentation> future = api.index();
      return future
        .then(index -> {
          if (index.getLinks().getLinkBy("login").isPresent()) {
            return FormValidation.ok();
          }
          return FormValidation.error("api has no login link");
        })
        .mapError(e -> FormValidation.error(e.getMessage()));
    }

    @SuppressWarnings("unused") // used By stapler
    public FormValidation doCheckCredentialsId(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value) throws InterruptedException {
      return validateCredentialsId(context, serverUrl, value, Authentications::new);
    }

    @VisibleForTesting
    FormValidation validateCredentialsId(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value, Function<SCMSourceOwner, Authentications> authenticationsProvider) throws InterruptedException {
      if (doCheckServerUrl(serverUrl).kind != FormValidation.Kind.OK) {
        return FormValidation.error("server url is required");
      }
      if (Strings.isNullOrEmpty(value)) {
        return FormValidation.error("credentials are required");
      }
      Authentications authentications = authenticationsProvider.apply(context);
      ScmManagerApi client = apiFactory.apply(serverUrl, authentications.from(serverUrl, value));
      Promise<HalRepresentation> future = client.index();
      return future.then(index -> {
        if (index.getLinks().getLinkBy("me").isPresent()) {
          return FormValidation.ok();
        }
        return FormValidation.error("login failed");
      }).mapError(e -> FormValidation.error(e.getMessage()));
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
    public ListBoxModel doFillRepositoryItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String credentialsId, @QueryParameter String value) throws InterruptedException {
      return fillRepositoryItems(context, serverUrl, credentialsId, value, Authentications::new);
    }

    public ListBoxModel fillRepositoryItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String credentialsId, @QueryParameter String value, Function<SCMSourceOwner, Authentications> authenticationsProvider) throws InterruptedException {
      ListBoxModel model = new ListBoxModel();
      if (Strings.isNullOrEmpty(serverUrl) || Strings.isNullOrEmpty(credentialsId)) {
        if (!Strings.isNullOrEmpty(value)) {
          model.add(value);
        }
        return model;
      }

      Authentications authentications = authenticationsProvider.apply(context);
      ScmManagerApi api = apiFactory.apply(serverUrl, authentications.from(serverUrl, credentialsId));
      List<Repository> repositories = api.getRepositories().mapError(e -> emptyList());
      for (Repository repository : repositories) {
        String displayName = String.format("%s/%s (%s)", repository.getNamespace(), repository.getName(), repository.getType());
        String v = String.format("%s/%s", repository.getNamespace(), repository.getName());
        model.add(displayName, v);
      }
      return model;
    }

    private static ScmManagerApi createHttpClient(String value, HttpAuthentication authentication) {
      return new ScmManagerApi(new ApiClient(value, authentication));
    }
  }
}
