package com.cloudogu.scmmanager.scm;

import hudson.plugins.mercurial.traits.CleanMercurialSCMSourceTrait;
import hudson.plugins.mercurial.traits.MercurialBrowserSCMSourceTrait;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.TagSCMHeadCategory;
import jenkins.scm.impl.UncategorizedSCMHeadCategory;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static com.cloudogu.scmmanager.scm.ScmTestData.context;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MercurialSCMBuilderProviderTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void shouldReturnMercurialSCMBuilderProvider() {
        SCMBuilderProvider provider = SCMBuilderProvider.byType("hg");
        assertThat(provider).isInstanceOf(MercurialSCMBuilderProvider.class);
        assertThat(provider.getType()).isEqualTo("hg");
    }

    @Test
    public void shouldSupportCategories() {
        SCMBuilderProvider provider = SCMBuilderProvider.byType("hg");
        assertThat(provider.isSupported(TagSCMHeadCategory.DEFAULT)).isFalse();
        assertThat(provider.isSupported(ChangeRequestSCMHeadCategory.DEFAULT)).isFalse();
        assertThat(provider.isSupported(UncategorizedSCMHeadCategory.DEFAULT)).isTrue();
    }

    @Test
    public void shouldCreateSCMBuilder() {
        SCMBuilderProvider provider = SCMBuilderProvider.byType("hg");
        SCMBuilder<?, ?> builder = provider.create(context("hg"));
        assertThat(builder).isInstanceOf(ScmManagerHgSCMBuilder.class);
    }

    @Test
    public void shouldFilterBrowserTrait() {
        SCMBuilderProvider provider = SCMBuilderProvider.byType("hg");
        boolean contains = provider.getTraitDescriptors(new ScmManagerSource.DescriptorImpl())
            .stream()
            .map(SCMSourceTraitDescriptor::getClass)
            .anyMatch(clazz -> clazz.equals(MercurialBrowserSCMSourceTrait.DescriptorImpl.class));
        assertThat(contains).isFalse();
    }

    @Test
    public void shouldContainCleanTrait() {
        SCMBuilderProvider provider = SCMBuilderProvider.byType("hg");
        boolean contains = provider.getTraitDescriptors(new ScmManagerSource.DescriptorImpl())
            .stream()
            .map(SCMSourceTraitDescriptor::getClass)
            .anyMatch(clazz -> clazz.equals(CleanMercurialSCMSourceTrait.DescriptorImpl.class));
        assertThat(contains).isTrue();
    }

}
