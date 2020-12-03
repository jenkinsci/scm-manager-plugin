package com.cloudogu.scmmanager.scm.jobdsl;

import com.cloudogu.scmmanager.scm.BranchDiscoveryTrait;
import com.cloudogu.scmmanager.scm.PullRequestDiscoveryTrait;
import com.cloudogu.scmmanager.scm.ScmManagerSource;
import com.cloudogu.scmmanager.scm.ScmTestData;
import com.cloudogu.scmmanager.scm.TagDiscoveryTrait;
import com.cloudogu.scmmanager.scm.api.ApiClient;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import javaposse.jobdsl.dsl.DslScriptException;
import jenkins.branch.BranchSource;
import jenkins.scm.api.trait.SCMSourceTrait;
import org.assertj.core.api.Condition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


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

    assertThat(source.getRepository()).isEqualTo("hitchhiker/hog/git");
    assertThat(source.getId()).isEqualTo("42");
    assertTraits(source, BranchDiscoveryTrait.class, PullRequestDiscoveryTrait.class, TagDiscoveryTrait.class);
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
    assertTraits(source, TagDiscoveryTrait.class);
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

    assertThat(source.getRepository()).isEqualTo("hitchhiker/hog/git");
  }


  @SafeVarargs
  private final void assertTraits(ScmManagerSource source, Class<? extends SCMSourceTrait>... expected) {
    Set<Class<? extends SCMSourceTrait>> traits = source.getTraits()
      .stream()
      .map(SCMSourceTrait::getClass)
      .collect(Collectors.toSet());
    assertThat(traits).containsOnly(expected);
  }

}
