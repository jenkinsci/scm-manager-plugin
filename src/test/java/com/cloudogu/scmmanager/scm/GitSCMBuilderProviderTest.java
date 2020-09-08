package com.cloudogu.scmmanager.scm;

import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.TagSCMHeadCategory;
import jenkins.scm.impl.UncategorizedSCMHeadCategory;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class GitSCMBuilderProviderTest {

  @Rule
  public JenkinsRule jenkinsRule = new JenkinsRule();

  @Test
  public void shouldReturnGitSCMBuilderProvider() {
    SCMBuilderProvider provider = SCMBuilderProvider.byType("git");
    assertThat(provider).isInstanceOf(GitSCMBuilderProvider.class);
    assertThat(provider.getType()).isEqualTo("git");
  }

  @Test
  public void shouldSupportCategories() {
    SCMBuilderProvider provider = SCMBuilderProvider.byType("git");
    assertThat(provider.isSupported(TagSCMHeadCategory.DEFAULT)).isTrue();
    assertThat(provider.isSupported(ChangeRequestSCMHeadCategory.DEFAULT)).isTrue();
    assertThat(provider.isSupported(UncategorizedSCMHeadCategory.DEFAULT)).isTrue();
  }

}
