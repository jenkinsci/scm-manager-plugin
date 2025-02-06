package com.cloudogu.scmmanager.scm;

import static java.util.Collections.emptyList;

import com.cloudogu.scmmanager.scm.api.IllegalReturnStatusException;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceOwner;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScmManagerSourceDescriptor extends SCMSourceDescriptor {

    protected final ScmManagerApiFactory apiFactory;
    private final Predicate<Repository> repositoryPredicate;
    private final Logger LOG = LoggerFactory.getLogger(ScmManagerSourceDescriptor.class);

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

    //  @SuppressWarnings("unused") // used By stapler
    //  public FormValidation doCheckRepository(@QueryParameter String value) throws InterruptedException,
    // ExecutionException {
    //    if (fillRepositoryItemsResult == null) {
    //      LOG.debug("No repository result to check");
    //      return FormValidation.ok();
    //    } else if (!Objects.equals(value, "") && !fillRepositoryItemsResult.model.contains(value)) {
    //      return FormValidation.error("This repository does not exist.");
    //    }
    //    return FormValidation.ok();
    //  }

    @VisibleForTesting
    FormValidation validateCredentialsId(
            @AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value)
            throws InterruptedException, ExecutionException {
        return ConnectionConfiguration.validateCredentialsId(apiFactory, context, serverUrl, value);
    }

    public FormValidation doCheckRepository(
            @AncestorInPath SCMSourceOwner context,
            @QueryParameter String serverUrl,
            @QueryParameter String credentialsId,
            @QueryParameter String value)
            throws InterruptedException, ExecutionException {
        System.out.println("doCheckRepository: " + value);
        if (Strings.isNullOrEmpty(serverUrl) || Strings.isNullOrEmpty(credentialsId) || Strings.isNullOrEmpty(value)) {
            return FormValidation.ok();
        }
        String[] namespaceNameParts = value.split("/");
        if (namespaceNameParts.length != 2) {
            return FormValidation.error(
                    "Please enter a valid repository in the form namespace/name or select one from the dropdown.");
        }
        try {
            ScmManagerApi api = apiFactory.create(context, serverUrl, credentialsId);
            api.getRepository(namespaceNameParts[0], namespaceNameParts[1]).get();
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

    @SuppressWarnings("unused") // used By stapler
    public ComboBoxModel doFillRepositoryItems(
            @AncestorInPath SCMSourceOwner context,
            @QueryParameter String serverUrl,
            @QueryParameter String credentialsId,
            @QueryParameter String value)
            throws InterruptedException, ExecutionException {
        return fillRepositoryItems(context, serverUrl, credentialsId, value);
    }

    public ComboBoxModel fillRepositoryItems(
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
