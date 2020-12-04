package com.cloudogu.scmmanager.scm.jobdsl;

import com.cloudogu.scmmanager.scm.BranchDiscoveryTrait;
import com.cloudogu.scmmanager.scm.PullRequestDiscoveryTrait;
import com.cloudogu.scmmanager.scm.ScmManagerNavigator;
import com.cloudogu.scmmanager.scm.ScmManagerSvnNavigatorTrait;
import com.cloudogu.scmmanager.scm.TagDiscoveryTrait;
import javaposse.jobdsl.dsl.DslScriptException;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static com.cloudogu.scmmanager.scm.jobdsl.Asserts.assertContainsOnlyInstancesOf;
import static org.assertj.core.api.Assertions.assertThat;

public class NavigatorExtensionTest {

  @Rule
  public JenkinsRule jenkinsRule = new JenkinsRule();

  @Test
  public void shouldCreateNavigator() {
    NavigatorExtension extension = new NavigatorExtension((runnable, ctx) -> {
      ScmManagerNavigatorContext context = (ScmManagerNavigatorContext) ctx;
      context.serverUrl("https://scm.hitchhiker.com/scm");
      context.credentialsId("secret");
      context.namespace("spaceships");
      context.discoverSvn(false);
    });

    ScmManagerNavigator scmNavigator = extension.scmManagerNamespace(null);
    assertThat(scmNavigator.getServerUrl()).isEqualTo("https://scm.hitchhiker.com/scm");
    assertThat(scmNavigator.getCredentialsId()).isEqualTo("secret");
    assertThat(scmNavigator.getNamespace()).isEqualTo("spaceships");
    assertContainsOnlyInstancesOf(scmNavigator.getTraits(),
      BranchDiscoveryTrait.class, PullRequestDiscoveryTrait.class
    );
  }

  @Test
  public void shouldDisableDefaultTraits() {
    NavigatorExtension extension = new NavigatorExtension((runnable, ctx) -> {
      ScmManagerNavigatorContext context = (ScmManagerNavigatorContext) ctx;
      context.serverUrl("https://scm.hitchhiker.com/scm");
      context.credentialsId("secret");
      context.namespace("spaceships");
      context.discoverBranches(false);
      context.discoverPullRequest(false);
      context.discoverTags(true);
      context.discoverSvn(true);
    });

    ScmManagerNavigator scmNavigator = extension.scmManagerNamespace(null);
    assertContainsOnlyInstancesOf(scmNavigator.getTraits(),
      TagDiscoveryTrait.class, ScmManagerSvnNavigatorTrait.class
    );
  }

  @Test
  public void shouldConfigureIncludesAndExcludesOfSubversionTrait() {
    NavigatorExtension extension = new NavigatorExtension((runnable, ctx) -> {
      if (ctx instanceof ScmManagerNavigatorContext) {
        ScmManagerNavigatorContext context = (ScmManagerNavigatorContext) ctx;
        context.serverUrl("https://scm.hitchhiker.com/scm");
        context.credentialsId("secret");
        context.namespace("spaceships");
        context.discoverBranches(false);
        context.discoverPullRequest(false);
        context.discoverSvn(() -> {});
      } else if (ctx instanceof ScmManagerNavigatorContext.SubversionContext) {
        ScmManagerNavigatorContext.SubversionContext context = (ScmManagerNavigatorContext.SubversionContext) ctx;
        context.includes("tags");
        context.excludes("tags/0.*");
      }
    });

    ScmManagerNavigator scmNavigator = extension.scmManagerNamespace(null);
    ScmManagerSvnNavigatorTrait trait = (ScmManagerSvnNavigatorTrait) scmNavigator.getTraits().get(0);
    assertThat(trait.getIncludes()).isEqualTo("tags");
    assertThat(trait.getExcludes()).isEqualTo("tags/0.*");
  }

  @Test(expected = DslScriptException.class)
  public void shouldFailWithoutNamespace() {
    NavigatorExtension extension = new NavigatorExtension((runnable, ctx) -> {
      ScmManagerNavigatorContext context = (ScmManagerNavigatorContext) ctx;
      context.serverUrl("https://scm.hitchhiker.com/scm");
      context.credentialsId("secret");
    });

    extension.scmManagerNamespace(null);
  }

}
