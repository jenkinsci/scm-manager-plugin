package com.cloudogu.scmmanager.scm.jobdsl;

import com.cloudogu.scmmanager.scm.PullRequestDiscoveryTrait;
import com.cloudogu.scmmanager.scm.ScmManagerBranchDiscoveryTrait;
import com.cloudogu.scmmanager.scm.ScmManagerSource;
import com.cloudogu.scmmanager.scm.ScmManagerSvnSource;
import com.cloudogu.scmmanager.scm.Subversion;
import com.cloudogu.scmmanager.scm.TagDiscoveryTrait;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import javaposse.jobdsl.dsl.DslScriptException;
import jenkins.branch.BranchSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.cloudogu.scmmanager.scm.jobdsl.Asserts.assertContainsOnlyInstancesOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BranchSourcesExtensionTest {

  @Rule
  public JenkinsRule jenkinsRule = new JenkinsRule();

  @Mock
  private ScmManagerApiFactory apiFactory;

  @Test
  public void shouldCreateSourceWithoutRepositoryFetch() throws ExecutionException, InterruptedException {
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
    assertContainsOnlyInstancesOf(source.getTraits(),
      ScmManagerBranchDiscoveryTrait.class, PullRequestDiscoveryTrait.class, TagDiscoveryTrait.class
    );
    verifyNoMoreInteractions(apiFactory);
  }

  @Test
  public void shouldDisableDefaultTraits() throws ExecutionException, InterruptedException {
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

  @Test(expected = DslScriptException.class)
  public void shouldFailIfIdIsRequiredFieldIsMissing() throws ExecutionException, InterruptedException {
    BranchSourcesExtension extension = new BranchSourcesExtension(apiFactory, (runnable, ctx) -> {
      ScmManagerBranchSourceContext context = (ScmManagerBranchSourceContext) ctx;
      context.credentialsId("secret");
      context.repository("hitchhiker/hog/git");
      context.serverUrl("https://scm.hitchhiker.com");
    });
    extension.scmManager(null);
  }

  @Test
  public void shouldCreateSourceAndFetchTypeFromApi() throws ExecutionException, InterruptedException {
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
    when(apiFactory.create(jenkinsRule.getInstance(), "https://scm.hitchhiker.com", "secret")).thenReturn(api);
    when(api.getRepository("hitchhiker", "hog")).thenReturn(CompletableFuture.completedFuture(
      repository
    ));

    BranchSource branchSource = extension.scmManager(null);
    ScmManagerSource source = (ScmManagerSource) branchSource.getSource();

    assertThat(source.getRepository()).isEqualTo("hitchhiker/hog (git)");
  }

  @Test
  public void shouldSetIncludesAndExcludes() {
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
  public void shouldCreateScmManagerSvnSource() {
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
