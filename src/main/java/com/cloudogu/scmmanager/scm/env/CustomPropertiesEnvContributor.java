package com.cloudogu.scmmanager.scm.env;

import com.cloudogu.scmmanager.scm.ScmManagerApiData;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import com.fasterxml.jackson.databind.JsonNode;
import de.otto.edison.hal.HalRepresentation;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.InvisibleAction;
import hudson.model.ItemGroup;
import hudson.model.Run;
import hudson.model.TaskListener;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Extension
public class CustomPropertiesEnvContributor extends EnvironmentContributor {

    private static final String ENV_PREFIX = "SCM_CUSTOM_PROP_";

    private final ScmManagerApiFactory apiFactory;

    public CustomPropertiesEnvContributor() {
        this(new ScmManagerApiFactory());
    }

    public CustomPropertiesEnvContributor(ScmManagerApiFactory apiFactory) {
        this.apiFactory = apiFactory;
    }

    @Override
    public void buildEnvironmentFor(@Nonnull Run run,
                                    @Nonnull EnvVars envs,
                                    @Nonnull TaskListener listener) {
        PrintStream logger = listener.getLogger();

        CustomPropertiesAction customProperties = run.getAction(CustomPropertiesAction.class);

        if (customProperties == null) {
            logger.println("[SCM-Manager Custom Properties] Fetch properties");
            customProperties = new CustomPropertiesAction(fetchProperties(
                run.getParent().getAction(ScmManagerApiData.class), run.getParent().getParent()
            ));
            run.addAction(customProperties);
        }

        customProperties.getProperties().forEach((key, value) -> envs.put(ENV_PREFIX + key, value));
    }

    private Map<String, String> fetchProperties(ScmManagerApiData apiData, ItemGroup<?> owner) {
        if (apiData == null) {
            return Collections.emptyMap();
        }

        ScmManagerApi client = apiFactory.create(owner, apiData.getServerUrl(), apiData.getCredentialsId());

        try {
            Repository repository = client.getRepository(apiData.getNamespace(), apiData.getName()).get();
            return parseProperties(repository);
        } catch (Exception e) {
            //TODO Add proper catch and error handling
        }

        return Collections.emptyMap();
    }

    private Map<String, String> parseProperties(Repository repository) {
        Map<String, String> properties = new HashMap<>();

        if (!repository.getEmbedded().hasItem("customProperties")) {
            return properties;
        }

        List<HalRepresentation> customPropertiesRelation = repository.getEmbedded().getItemsBy("customProperties");
        if (customPropertiesRelation.isEmpty()) {
            return properties;
        }

        JsonNode props = customPropertiesRelation.get(0).getAttribute("properties");
        props.elements().forEachRemaining(
            prop -> properties.put(prop.get("key").asText(), prop.get("value").asText())
        );

        return properties;
    }

    @Getter
    static class CustomPropertiesAction extends InvisibleAction {
        private final Map<String, String> properties;

        CustomPropertiesAction(Map<String, String> properties) {
            this.properties = properties;
        }
    }
}
