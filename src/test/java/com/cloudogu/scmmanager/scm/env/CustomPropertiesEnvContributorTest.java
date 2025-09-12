package com.cloudogu.scmmanager.scm.env;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.cloudogu.scmmanager.scm.ScmManagerApiData;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import hudson.EnvVars;
import hudson.model.TaskListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomPropertiesEnvContributorTest {

    private static final String SERVER_URL = "http://localhost:8080";
    private static final String CREDENTIALS_ID = "CREDS_ID";
    private static final String NAMESPACE = "NAMESPACE";
    private static final String NAME = "NAME";

    @Mock
    private ScmManagerApiFactory apiFactory;

    @Mock
    private ScmManagerApi api;

    @Mock
    private Jenkins owner;

    @Mock
    private WorkflowJob job;

    @Mock
    private WorkflowRun run;

    @Mock
    private EnvVars envVars;

    @Mock
    private TaskListener listener;

    private CustomPropertiesEnvContributor envContributor;

    @Before
    public void setup() {
        lenient().when(job.getParent()).thenReturn(owner);
        lenient().when(run.getParent()).thenReturn(job);
        lenient().when(listener.getLogger()).thenReturn(System.out);

        envContributor = new CustomPropertiesEnvContributor(apiFactory);
    }

    private void setupApiCall(CompletableFuture<Repository> result) {
        when(job.getAction(ScmManagerApiData.class))
                .thenReturn(new ScmManagerApiData(SERVER_URL, CREDENTIALS_ID, NAMESPACE, NAME));
        when(apiFactory.create(owner, SERVER_URL, CREDENTIALS_ID)).thenReturn(api);
        when(api.getRepository(NAMESPACE, NAME)).thenReturn(result);
    }

    @Test
    public void shouldInjectCustomPropertiesFromCachedAction() {
        when(run.getAction(CustomPropertiesEnvContributor.CustomPropertiesAction.class))
                .thenReturn(new CustomPropertiesEnvContributor.CustomPropertiesAction(
                        Map.of("lang", "java", "version", "17")));

        envContributor.buildEnvironmentFor(run, envVars, listener);

        verify(envVars).put("SCMM_CUSTOM_PROP_lang", "java");
        verify(envVars).put("SCMM_CUSTOM_PROP_version", "17");
        verifyNoMoreInteractions(envVars);
    }

    @Test
    public void shouldNotInjectBecauseApiDataIsMissing() {
        envContributor.buildEnvironmentFor(run, envVars, listener);

        verifyNoInteractions(envVars);
    }

    @Test
    public void shouldNotInjectBecauseFetchFailed() {
        setupApiCall(CompletableFuture.failedFuture(new RuntimeException()));

        envContributor.buildEnvironmentFor(run, envVars, listener);

        verifyNoInteractions(envVars);
        verify(api).getRepository(NAMESPACE, NAME);
    }

    @Test
    public void shouldNotInjectBecauseCustomPropertiesAreNotEmbedded() {
        setupApiCall(CompletableFuture.completedFuture(new Repository(NAMESPACE, NAME, "git")));

        envContributor.buildEnvironmentFor(run, envVars, listener);

        verifyNoInteractions(envVars);
        verify(api).getRepository(NAMESPACE, NAME);
    }

    @Test
    public void shouldNotInjectBecauseCustomPropertiesAreEmpty() {
        Repository repo = new Repository(NAMESPACE, NAME, "git", Embedded.embedded("customProperties", List.of()));
        setupApiCall(CompletableFuture.completedFuture(repo));
        envContributor.buildEnvironmentFor(run, envVars, listener);

        verifyNoInteractions(envVars);
        verify(api).getRepository(NAMESPACE, NAME);
    }

    @Test
    public void shouldNotInjectBecauseNoPropertiesAddedToRepository() {
        Repository repo = new Repository(
                NAMESPACE, NAME, "git", Embedded.embedded("customProperties", List.of(new Wrapper(List.of()))));
        setupApiCall(CompletableFuture.completedFuture(repo));

        envContributor.buildEnvironmentFor(run, envVars, listener);

        verifyNoInteractions(envVars);
        verify(api).getRepository(NAMESPACE, NAME);
    }

    @Test
    public void shouldInjectFromFetchedRepository() {
        Repository repo = new Repository(
                NAMESPACE,
                NAME,
                "git",
                Embedded.embedded(
                        "customProperties",
                        List.of(new Wrapper(List.of(
                                Map.of("key", "lang", "value", "java"), Map.of("key", "version", "value", "17"))))));
        setupApiCall(CompletableFuture.completedFuture(repo));

        envContributor.buildEnvironmentFor(run, envVars, listener);

        verify(envVars).put("SCMM_CUSTOM_PROP_lang", "java");
        verify(envVars).put("SCMM_CUSTOM_PROP_version", "17");
        verifyNoMoreInteractions(envVars);
    }

    static class Wrapper extends HalRepresentation {
        private final List<Map<String, String>> properties;

        public Wrapper(List<Map<String, String>> properties) {
            this.properties = properties;
        }

        @Override
        public JsonNode getAttribute(String attributeName) {
            if (attributeName.equals("properties")) {
                return new ObjectMapper().valueToTree(properties);
            }

            return null;
        }
    }
}
