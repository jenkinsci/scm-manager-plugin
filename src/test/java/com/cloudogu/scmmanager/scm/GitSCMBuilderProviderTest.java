package com.cloudogu.scmmanager.scm;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.TagSCMHeadCategory;
import jenkins.scm.impl.UncategorizedSCMHeadCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class GitSCMBuilderProviderTest {

    private JenkinsRule j;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        j = rule;
    }

    @Test
    void shouldReturnGitSCMBuilderProvider() {
        SCMBuilderProvider provider = SCMBuilderProvider.byType("git");
        assertThat(provider).isInstanceOf(GitSCMBuilderProvider.class);
        assertThat(provider.getType()).isEqualTo("git");
    }

    @Test
    void shouldSupportCategories() {
        SCMBuilderProvider provider = SCMBuilderProvider.byType("git");
        assertThat(provider.isSupported(TagSCMHeadCategory.DEFAULT)).isTrue();
        assertThat(provider.isSupported(ChangeRequestSCMHeadCategory.DEFAULT)).isTrue();
        assertThat(provider.isSupported(UncategorizedSCMHeadCategory.DEFAULT)).isTrue();
    }
}
