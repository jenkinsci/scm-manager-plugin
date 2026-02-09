package com.cloudogu.scmmanager.scm.jobdsl;

import static com.cloudogu.scmmanager.scm.jobdsl.Asserts.assertContainsOnlyInstancesOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.cloudogu.scmmanager.scm.PullRequestDiscoveryTrait;
import com.cloudogu.scmmanager.scm.ScmManagerBranchDiscoveryTrait;
import com.cloudogu.scmmanager.scm.ScmManagerSource;
import com.cloudogu.scmmanager.scm.ScmManagerSvnSource;
import com.cloudogu.scmmanager.scm.Subversion;
import com.cloudogu.scmmanager.scm.TagDiscoveryTrait;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import java.util.concurrent.CompletableFuture;
import javaposse.jobdsl.dsl.DslScriptException;
import jenkins.branch.BranchSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@WithJenkins
class BranchSourcesExtensionTest {

    @Mock
    private ScmManagerApiFactory apiFactory;

    private JenkinsRule j;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        j = rule;
    }

    @Test
    void shouldCreateSourceWithoutRepositoryFetch() throws Exception {
        BranchSourcesExtension extension = new BranchSourcesExtension(apiFactory, (runnable, ctx) -> {
            ScmManagerBranchSourceContext context = (ScmManagerBranchSourceContext) ctx;
            context.id("42");
            context.credentialsId("secret");
            context.repository("hitchhiker/hog/git");
            context.serverUrl("https://scm.hitchhiker.com");
            context.discoverTags(true);
        });
        BranchSource branchSource = extension.scmManager(null);
        ScmManagerSource source = (ScmManagerSource) branchSource.getSource();

        assertThat(source.getRepository()).isEqualTo("hitchhiker/hog (git)");
        assertThat(source.getId()).isEqualTo("42");
        assertContainsOnlyInstancesOf(
                source.getTraits(),
                ScmManagerBranchDiscoveryTrait.class,
                PullRequestDiscoveryTrait.class,
                TagDiscoveryTrait.class);
        verifyNoMoreInteractions(apiFactory);
    }

    @Test
    void shouldDisableDefaultTraits() throws Exception {
        BranchSourcesExtension extension = new BranchSourcesExtension(apiFactory, (runnable, ctx) -> {
            ScmManagerBranchSourceContext context = (ScmManagerBranchSourceContext) ctx;
            context.id("42");
            context.credentialsId("secret");
            context.repository("hitchhiker/hog/git");
            context.serverUrl("https://scm.hitchhiker.com");
            context.discoverBranches(false);
            context.discoverPullRequest(false);
            context.discoverTags(true);
        });
        BranchSource branchSource = extension.scmManager(null);
        ScmManagerSource source = (ScmManagerSource) branchSource.getSource();
        assertContainsOnlyInstancesOf(source.getTraits(), TagDiscoveryTrait.class);
    }

    @Test
    void shouldFailIfIdIsRequiredFieldIsMissing() {
        BranchSourcesExtension extension = new BranchSourcesExtension(apiFactory, (runnable, ctx) -> {
            ScmManagerBranchSourceContext context = (ScmManagerBranchSourceContext) ctx;
            context.credentialsId("secret");
            context.repository("hitchhiker/hog/git");
            context.serverUrl("https://scm.hitchhiker.com");
        });
        assertThrows(DslScriptException.class, () -> extension.scmManager(null));
    }

    @Test
    void shouldCreateSourceAndFetchTypeFromApi() throws Exception {
        BranchSourcesExtension extension = new BranchSourcesExtension(apiFactory, (runnable, ctx) -> {
            ScmManagerBranchSourceContext context = (ScmManagerBranchSourceContext) ctx;
            context.id("42");
            context.credentialsId("secret");
            context.repository("hitchhiker/hog");
            context.serverUrl("https://scm.hitchhiker.com");
        });

        ScmManagerApi api = mock(ScmManagerApi.class);
        Repository repository = mock(Repository.class);
        when(repository.getType()).thenReturn("git");
        when(apiFactory.create(j.getInstance(), "https://scm.hitchhiker.com", "secret"))
                .thenReturn(api);
        when(api.getRepository("hitchhiker", "hog")).thenReturn(CompletableFuture.completedFuture(repository));

        BranchSource branchSource = extension.scmManager(null);
        ScmManagerSource source = (ScmManagerSource) branchSource.getSource();

        assertThat(source.getRepository()).isEqualTo("hitchhiker/hog (git)");
    }

    @Test
    void shouldSetIncludesAndExcludes() {
        BranchSourcesExtension extension = new BranchSourcesExtension(apiFactory, (runnable, ctx) -> {
            ScmManagerSvnBranchSourceContext context = (ScmManagerSvnBranchSourceContext) ctx;
            context.id("42");
            context.credentialsId("secret");
            context.repository("hitchhiker/hog/git");
            context.serverUrl("https://scm.hitchhiker.com");
            context.includes("tags");
            context.excludes("tags/0.*");
        });
        BranchSource branchSource = extension.scmManagerSvn(null);
        ScmManagerSvnSource source = (ScmManagerSvnSource) branchSource.getSource();
        assertThat(source.getIncludes()).isEqualTo("tags");
        assertThat(source.getExcludes()).isEqualTo("tags/0.*");
    }

    @Test
    void shouldCreateScmManagerSvnSource() {
        BranchSourcesExtension extension = new BranchSourcesExtension(apiFactory, (runnable, ctx) -> {
            ScmManagerSvnBranchSourceContext context = (ScmManagerSvnBranchSourceContext) ctx;
            context.id("42");
            context.credentialsId("secret");
            context.repository("hitchhiker/hog/git");
            context.serverUrl("https://scm.hitchhiker.com");
        });
        BranchSource branchSource = extension.scmManagerSvn(null);
        ScmManagerSvnSource source = (ScmManagerSvnSource) branchSource.getSource();
        assertThat(source.getId()).isEqualTo("42");
        assertThat(source.getCredentialsId()).isEqualTo("secret");
        assertThat(source.getServerUrl()).isEqualTo("https://scm.hitchhiker.com");
        assertThat(source.getCredentialsId()).isEqualTo("secret");
        assertThat(source.getIncludes()).isEqualTo(Subversion.DEFAULT_INCLUDES);
        assertThat(source.getExcludes()).isEqualTo(Subversion.DEFAULT_EXCLUDES);
    }
}
