package com.cloudogu.scmmanager.scm;

import static java.util.Collections.emptyList;

import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.util.List;
import java.util.Objects;
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

    @SuppressWarnings("unused") // used By stapler
    public FormValidation doCheckRepository(@QueryParameter String value)
            throws InterruptedException, ExecutionException {
        if (fillRepositoryItemsResult == null) {
            LOG.debug("No repository result to check");
            return FormValidation.ok();
        } else if (!Objects.equals(value, "") && !fillRepositoryItemsResult.model.contains(value)) {
            return FormValidation.error("This repository does not exist.");
        }
        return FormValidation.ok();
    }

    @VisibleForTesting
    FormValidation validateCredentialsId(
            @AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value)
            throws InterruptedException, ExecutionException {
        return ConnectionConfiguration.validateCredentialsId(apiFactory, context, serverUrl, value);
    }

    @SuppressWarnings("unused") // used By stapler
    public ListBoxModel doFillCredentialsIdItems(
            @AncestorInPath SCMSourceOwner context, @QueryParameter String serverUrl, @QueryParameter String value) {
        return ConnectionConfiguration.fillCredentialsIdItems(context, serverUrl, value);
    }

    private record FillRepositoryItemsResult(ComboBoxModel model) {}

    private FillRepositoryItemsResult fillRepositoryItemsResult;

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

        if (Strings.isNullOrEmpty(serverUrl) || Strings.isNullOrEmpty(credentialsId)) {
            ComboBoxModel model = new ComboBoxModel();
            if (!Strings.isNullOrEmpty(value)) {
                model.add(value);
            }
            return model;
        }

        ScmManagerApi api = apiFactory.create(context, serverUrl, credentialsId);
        ComboBoxModel result = fetchRepositoryItems(api);
        fillRepositoryItemsResult = new FillRepositoryItemsResult(result);
        return fetchRepositoryItems(api);
    }

    private ComboBoxModel fetchRepositoryItems(ScmManagerApi api) throws ExecutionException, InterruptedException {
        ComboBoxModel model = new ComboBoxModel();

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
        return String.format("%s/%s (%s)", repository.getNamespace(), repository.getName(), repository.getType());
    }

    static {
        Icons.register("icon-scm-manager-source");
    }

    @Override
    public String getIconClassName() {
        return "icon-scm-manager-source";
    }
}
