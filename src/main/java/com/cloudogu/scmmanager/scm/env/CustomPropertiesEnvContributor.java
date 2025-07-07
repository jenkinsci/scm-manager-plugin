package com.cloudogu.scmmanager.scm.env;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.InvisibleAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

@Extension
public class CustomPropertiesEnvContributor extends EnvironmentContributor {

    public CustomPropertiesEnvContributor() {
    }

    @Override
    public void buildEnvironmentFor(@Nonnull Run run,
                                    @Nonnull EnvVars envs,
                                    @Nonnull TaskListener listener) throws IOException, InterruptedException {
        PrintStream logger = listener.getLogger();

        CustomPropertiesAction customProperties = run.getAction(CustomPropertiesAction.class);
        run.getParent().get

        if (customProperties == null) {
            logger.println("[SCM-Manager Custom Properties] Fetch properties");
            Map<String, String> props = new HashMap<>();
            props.put("HELLO", "WORLD");
            customProperties = new CustomPropertiesAction(props);
            run.addAction(customProperties);
        } else {
            logger.println("[SCM-Manager Custom Properties] Use cached properties");
        }

        customProperties.getProperties().forEach((key, value) -> {
            logger.printf("[SCM-Manager Custom Properties] Inject key '%s' with value '%s'\n", key, value);
            envs.put(key, value);
        });
    }

    @Getter
    static class CustomPropertiesAction extends InvisibleAction {
        private final Map<String, String> properties;

        CustomPropertiesAction(Map<String, String> properties) {
            this.properties = properties;
        }
    }
}
