package com.cloudogu.scmmanager.scm;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudogu.scmmanager.scm.api.ApiClient;
import com.cloudogu.scmmanager.scm.api.ApiClient.Promise;
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
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

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

    static Function<String, ApiClient> apiClientFactory = DescriptorImpl::createHttpClient;

    @Nonnull
    @Override
    public String getDisplayName() {
      return "SCM-Manager";
    }

    @SuppressWarnings("unused") // used By stapler
    public static FormValidation doCheckServerUrl(@QueryParameter String value) throws InterruptedException {
      String trimmedValue = value.trim();
      if (Strings.isNullOrEmpty(trimmedValue)) {
        return FormValidation.error("server url is required");
      }

      ApiClient client = apiClientFactory.apply(value);
      Promise<HalRepresentation> future = client.get("/api/v2", "application/vnd.scmm-index+json;v=2", HalRepresentation.class);
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
    public ListBoxModel doFillRepositoryItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String credentialsId, @QueryParameter String value) throws ExecutionException, InterruptedException {
      System.out.println(serverUrl);
      return new ListBoxModel();
    }

    private static ApiClient createHttpClient(@QueryParameter String value) {
      return new ApiClient(value);
    }
  }
}
