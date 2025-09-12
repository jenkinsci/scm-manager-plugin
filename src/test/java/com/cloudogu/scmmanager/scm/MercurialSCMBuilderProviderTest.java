package com.cloudogu.scmmanager.scm;

import static com.cloudogu.scmmanager.scm.ScmTestData.context;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import hudson.plugins.mercurial.traits.CleanMercurialSCMSourceTrait;
import hudson.plugins.mercurial.traits.MercurialBrowserSCMSourceTrait;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.TagSCMHeadCategory;
import jenkins.scm.impl.UncategorizedSCMHeadCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class MercurialSCMBuilderProviderTest {

    private JenkinsRule j;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        j = rule;
    }

    @Test
    void shouldReturnMercurialSCMBuilderProvider() {
        SCMBuilderProvider provider = SCMBuilderProvider.byType("hg");
        assertThat(provider).isInstanceOf(MercurialSCMBuilderProvider.class);
        assertThat(provider.getType()).isEqualTo("hg");
    }

    @Test
    void shouldSupportCategories() {
        SCMBuilderProvider provider = SCMBuilderProvider.byType("hg");
        assertThat(provider.isSupported(TagSCMHeadCategory.DEFAULT)).isFalse();
        assertThat(provider.isSupported(ChangeRequestSCMHeadCategory.DEFAULT)).isFalse();
        assertThat(provider.isSupported(UncategorizedSCMHeadCategory.DEFAULT)).isTrue();
    }

    @Test
    void shouldCreateSCMBuilder() {
        SCMBuilderProvider provider = SCMBuilderProvider.byType("hg");
        SCMBuilder<?, ?> builder = provider.create(context("hg"));
        assertThat(builder).isInstanceOf(ScmManagerHgSCMBuilder.class);
    }

    @Test
    void shouldFilterBrowserTrait() {
        SCMBuilderProvider provider = SCMBuilderProvider.byType("hg");
        boolean contains = provider.getTraitDescriptors(new ScmManagerSource.DescriptorImpl()).stream()
                .map(SCMSourceTraitDescriptor::getClass)
                .anyMatch(clazz -> clazz.equals(MercurialBrowserSCMSourceTrait.DescriptorImpl.class));
        assertThat(contains).isFalse();
    }

    @Test
    void shouldContainCleanTrait() {
        SCMBuilderProvider provider = SCMBuilderProvider.byType("hg");
        boolean contains = provider.getTraitDescriptors(new ScmManagerSource.DescriptorImpl()).stream()
                .map(SCMSourceTraitDescriptor::getClass)
                .anyMatch(clazz -> clazz.equals(CleanMercurialSCMSourceTrait.DescriptorImpl.class));
        assertThat(contains).isTrue();
    }
}
