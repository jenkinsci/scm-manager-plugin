package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.IllegalReturnStatusException;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceOwner;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

public class ScmManagerSourceDescriptor extends SCMSourceDescriptor {

    protected final ScmManagerApiFactory apiFactory;
    private final Predicate<Repository> repositoryPredicate;

    @VisibleForTesting
    ScmManagerSourceDescriptor(ScmManagerApiFactory apiFactory, Predicate<Repository> repositoryPredicate) {
        this.apiFactory = apiFactory;
        this.repositoryPredicate = repositoryPredicate;
    }

    @SuppressWarnings("unused") // used By stapler
    public FormValidation doCheckServerUrl(@QueryParameter String value)
            throws InterruptedException, ExecutionException {
        return ConnectionConfiguration.checkServerUrl(apiFactory, value);
    }

    @SuppressWarnings("unused") // used By stapler
    public FormValidation doCheckCredentialsId(
            @AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value)
            throws InterruptedException, ExecutionException {
        return validateCredentialsId(context, serverUrl, value);
    }

    @VisibleForTesting
    FormValidation validateCredentialsId(
            @AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value)
            throws InterruptedException, ExecutionException {
        return ConnectionConfiguration.validateCredentialsId(apiFactory, context, serverUrl, value);
    }

    @POST
    public FormValidation doCheckRepository(
            @AncestorInPath SCMSourceOwner context,
            @QueryParameter String serverUrl,
            @QueryParameter String credentialsId,
            @QueryParameter String value)
            throws InterruptedException {
        if (Strings.isNullOrEmpty(serverUrl) || Strings.isNullOrEmpty(credentialsId) || Strings.isNullOrEmpty(value)) {
            return FormValidation.ok();
        }
        RepositoryRepresentationUtil.RepositoryRepresentation repositoryRepresentation =
                RepositoryRepresentationUtil.parse(value);
        try {
            ScmManagerApi api = apiFactory.create(context, serverUrl, credentialsId);
            api.getRepository(repositoryRepresentation.namespace(), repositoryRepresentation.name())
                    .get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IllegalReturnStatusException
                    && ((IllegalReturnStatusException) e.getCause()).getStatusCode() == 404) {
                return FormValidation.error("This repository does not exist.");
            }
            return FormValidation.error("Error checking repository: " + e.getMessage());
        }
        return FormValidation.ok();
    }

    @SuppressWarnings("unused") // used By stapler
    public ListBoxModel doFillCredentialsIdItems(
            @AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value) {
        return ConnectionConfiguration.fillCredentialsIdItems(context, serverUrl, value);
    }

    @POST
    @SuppressWarnings("unused") // used By stapler
    public ComboBoxModel doFillRepositoryItems(
            @AncestorInPath SCMSourceOwner context,
            @QueryParameter String serverUrl,
            @QueryParameter String credentialsId,
            @QueryParameter String value)
            throws InterruptedException, ExecutionException {
        ComboBoxModel model = new ComboBoxModel();
        if (Strings.isNullOrEmpty(serverUrl) || Strings.isNullOrEmpty(credentialsId)) {
            if (!Strings.isNullOrEmpty(value)) {
                model.add(value);
            }
            return model;
        }

        ScmManagerApi api = apiFactory.create(context, serverUrl, credentialsId);
        // filter all repositories, which does not support the protocol
        Predicate<Repository> protocolPredicate =
                repository -> repository.getUrl(api.getProtocol()).isPresent();
        Predicate<Repository> predicate = protocolPredicate.and(repositoryPredicate);
        List<Repository> repositories =
                api.getRepositories().exceptionally(e -> emptyList()).get();
        for (Repository repository : repositories) {
            if (predicate.test(repository)) {
                String option = createRepositoryOption(repository);
                if (option != null) {
                    model.add(option);
                }
            }
        }
        return model;
    }

    protected String createRepositoryOption(Repository repository) {
        return RepositoryRepresentationUtil.format(repository);
    }

    static {
        Icons.register("icon-scm-manager-source");
    }

    @Override
    public String getIconClassName() {
        return "icon-scm-manager-source";
    }
}
