package com.cloudogu.scmmanager.scm;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudogu.scmmanager.scm.api.IllegalReturnStatusException;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.base.Strings;
import de.otto.edison.hal.HalRepresentation;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMSourceOwner;
import org.acegisecurity.Authentication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class ConnectionConfiguration {

  static ListBoxModel fillCredentialsIdItems(SCMSourceOwner context, String serverUrl, String value) {
    if (context == null || !context.hasPermission(Item.CONFIGURE)) {
      return new StandardUsernameListBoxModel().includeCurrentValue(value);
    }
    Authentication authentication = context instanceof Queue.Task
      ? ((Queue.Task) context).getDefaultAuthentication()
      : ACL.SYSTEM;
    return new StandardUsernameListBoxModel()
      .includeEmptyValue()
      .includeAs(authentication, context, findSupportedCredentials(serverUrl), URIRequirementBuilder.fromUri(value).build())
      .includeCurrentValue(value);
  }

  private static Class<? extends StandardUsernameCredentials> findSupportedCredentials(String serverUrl) {
    Class<? extends StandardUsernameCredentials> supportedCredentials;
    if (serverUrl.startsWith("ssh")) {
      supportedCredentials = StandardUsernameCredentials.class;
    } else {
      // fallback for http
      supportedCredentials = StandardUsernamePasswordCredentials.class;
    }
    return supportedCredentials;
  }

  static FormValidation validateCredentialsId(ScmManagerApiFactory apiFactory, SCMSourceOwner context, String serverUrl, String value) throws InterruptedException, ExecutionException {
    if (checkServerUrl(apiFactory, serverUrl).kind != FormValidation.Kind.OK) {
      return FormValidation.error("server url is required");
    }
    if (Strings.isNullOrEmpty(value)) {
      return FormValidation.error("credentials are required");
    }
    ScmManagerApi client;
    try {
      client = apiFactory.create(context, serverUrl, value);
    } catch (CredentialsUnavailableException e) {
      return FormValidation.error("credentials not valid for connection type");
    }
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

  static FormValidation checkServerUrl(ScmManagerApiFactory apiFactory, String value) throws InterruptedException, ExecutionException {
    String trimmedValue = value.trim();
    if (Strings.isNullOrEmpty(trimmedValue)) {
      return FormValidation.error("server url is required");
    }
    try {
      URI uri = new URI(value);
      if (!uri.isAbsolute()) {
        return FormValidation.error("illegal URL format");
      }
      String scheme = uri.getScheme();
      if (!scheme.startsWith("http") && !scheme.startsWith("ssh")) {
        return FormValidation.error("Only http, https or ssh urls accepted");
      }
    } catch (URISyntaxException e) {
      return FormValidation.error("illegal URL format");
    }

    // only http allows anonymous check, so we skip the check for all other supported schemes
    if (!value.startsWith("http")) {
      return FormValidation.ok();
    }

    ScmManagerApi api = apiFactory.anonymous(value);
    CompletableFuture<HalRepresentation> future = api.index();
    return future
      .thenApply(index -> {
        if (index.getLinks().getLinkBy("login").isPresent()) {
          return FormValidation.ok();
        }
        return FormValidation.error("api has no login link");
      })
      .exceptionally(e -> {
        if (e.getCause() instanceof IllegalReturnStatusException && ((IllegalReturnStatusException) e.getCause()).getStatusCode() == 302) {
          return FormValidation.ok("Credentials needed");
        } else if (e.getCause() instanceof JsonParseException) {
          return FormValidation.error(
            "This does not seem to be a valid SCM-Manager url, or this is not the root URL of SCM-Manager " +
              "(maybe you have specified 'http://my-scm-server.org/scm/repos' instead of 'http://my-scm-server.org/scm/'.");
        }
        return FormValidation.error(e.getMessage());
      })
      .get();
  }
}
