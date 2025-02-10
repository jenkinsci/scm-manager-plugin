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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import jenkins.scm.api.SCMSourceOwner;
import org.acegisecurity.Authentication;

class ConnectionConfiguration {

    static final String SERVER_URL_IS_REQUIRED = "Server URL is required.";
    static final String CREDENTIALS_ARE_REQUIRED = "Credentials are required.";
    static final String CREDENTIALS_NOT_VALID_FOR_CONNECTION_TYPE =
            "The credentials are not valid for this connection type.";
    static final String LOGIN_FAILED = "Login has failed.";
    static final String ILLEGAL_URL_FORMAT = "Illegal URL format.";
    static final String ONLY_HTTPS_OR_SSH_URLS_ACCEPTED = "Only HTTP, HTTPS or SSH URLs are accepted.";
    static final String API_HAS_NO_LOGIN_LINK = "The API of this URL has no login link.";
    static final String MESSAGE = """
        This does not seem to be a valid SCM-Manager URL, or this is not the root URL of SCM-Manager.
        Maybe you have specified 'http://my-scm-server.org/scm/repos' instead of 'http://my-scm-server.org/scm/'.
        """;
    static final String CREDENTIALS_NEEDED = "Credentials needed.";

    static ListBoxModel fillCredentialsIdItems(SCMSourceOwner context, String serverUrl, String value) {
        if (context == null || !context.hasPermission(Item.CONFIGURE)) {
            return new StandardUsernameListBoxModel().includeCurrentValue(value);
        }
        Authentication authentication =
                context instanceof Queue.Task ? ((Queue.Task) context).getDefaultAuthentication() : ACL.SYSTEM;
        return new StandardUsernameListBoxModel()
                .includeEmptyValue()
                .includeAs(
                        authentication,
                        context,
                        findSupportedCredentials(serverUrl),
                        URIRequirementBuilder.fromUri(value).build())
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

    static FormValidation validateCredentialsId(
            ScmManagerApiFactory apiFactory, SCMSourceOwner context, String serverUrl, String value)
            throws InterruptedException, ExecutionException {
        if (checkServerUrl(apiFactory, serverUrl).kind != FormValidation.Kind.OK) {
            return FormValidation.error(SERVER_URL_IS_REQUIRED);
        }
        if (Strings.isNullOrEmpty(value)) {
            return FormValidation.error(CREDENTIALS_ARE_REQUIRED);
        }
        ScmManagerApi client;
        try {
            client = apiFactory.create(context, serverUrl, value);
        } catch (CredentialsUnavailableException e) {
            return FormValidation.error(CREDENTIALS_NOT_VALID_FOR_CONNECTION_TYPE);
        }
        CompletableFuture<HalRepresentation> future = client.index();
        return future.thenApply(index -> {
                    if (index.getLinks().getLinkBy("me").isPresent()) {
                        return FormValidation.ok();
                    }
                    return FormValidation.error(LOGIN_FAILED);
                })
                .exceptionally(e -> FormValidation.error(e.getMessage()))
                .get();
    }

    static FormValidation checkServerUrl(ScmManagerApiFactory apiFactory, String value)
            throws InterruptedException, ExecutionException {
        String trimmedValue = value == null ? null : value.trim();
        if (Strings.isNullOrEmpty(trimmedValue)) {
            return FormValidation.error(SERVER_URL_IS_REQUIRED);
        }
        try {
            URI uri = new URI(value);
            if (!uri.isAbsolute()) {
                return FormValidation.error(ILLEGAL_URL_FORMAT);
            }
            String scheme = uri.getScheme();
            if (!scheme.startsWith("http") && !scheme.startsWith("ssh")) {
                return FormValidation.error(ONLY_HTTPS_OR_SSH_URLS_ACCEPTED);
            }
        } catch (URISyntaxException e) {
            return FormValidation.error(ILLEGAL_URL_FORMAT);
        }

        // only http allows anonymous check, so we skip the check for all other supported schemes
        if (!value.startsWith("http")) {
            return FormValidation.ok();
        }

        ScmManagerApi api = apiFactory.anonymous(value);
        CompletableFuture<HalRepresentation> future = api.index();
        return future.thenApply(index -> {
                    if (index.getLinks().getLinkBy("login").isPresent()) {
                        return FormValidation.ok();
                    }
                    return FormValidation.error(API_HAS_NO_LOGIN_LINK);
                })
                .exceptionally(e -> {
                    if (e.getCause() instanceof IllegalReturnStatusException
                            && ((IllegalReturnStatusException) e.getCause()).getStatusCode() == 302) {
                        return FormValidation.ok(CREDENTIALS_NEEDED);
                    } else if (e.getCause() instanceof JsonParseException) {
                        return FormValidation.error(MESSAGE);
                    }
                    return FormValidation.error(e.getMessage());
                })
                .get();
    }
}
