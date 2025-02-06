package com.cloudogu.scmmanager.scm;

import static com.cloudogu.scmmanager.scm.ScmTestData.context;
import static org.assertj.core.api.Assertions.assertThat;

import com.cloudogu.scmmanager.scm.api.Repository;
import jenkins.scm.api.trait.SCMBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class SCMBuilderProviderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void shouldSupportGitRepository() {
        Repository repository = new Repository("hitchhiker", "heartOfGold", "git");
        assertThat(SCMBuilderProvider.isSupported(repository)).isTrue();
    }

    @Test
    public void shouldSupportMercurialRepository() {
        Repository repository = new Repository("hitchhiker", "heartOfGold", "hg");
        assertThat(SCMBuilderProvider.isSupported(repository)).isTrue();
    }

    @Test
    public void shouldNotSupportUnknownRepository() {
        Repository repository = new Repository("hitchhiker", "heartOfGold", "bazar");
        assertThat(SCMBuilderProvider.isSupported(repository)).isFalse();
    }

    @Test
    public void shouldCreateGitBuilder() {
        SCMBuilderProvider.Context context = context("git");
        SCMBuilder<?, ?> builder = SCMBuilderProvider.from(context);
        assertThat(builder).isInstanceOf(ScmManagerGitSCMBuilder.class);
    }

    @Test
    public void shouldCreateMercurialBuilder() {
        SCMBuilderProvider.Context context = context("hg");
        SCMBuilder<?, ?> builder = SCMBuilderProvider.from(context);
        assertThat(builder).isInstanceOf(ScmManagerHgSCMBuilder.class);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailToCreateContextForUnknownType() {
        SCMBuilderProvider.from(context("tsf"));
    }
}
