package com.cloudogu.scmmanager.scm;

import static com.cloudogu.scmmanager.scm.ScmTestData.context;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloudogu.scmmanager.scm.api.Repository;
import jenkins.scm.api.trait.SCMBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class SCMBuilderProviderTest {

    private JenkinsRule j;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        j = rule;
    }

    @Test
    void shouldSupportGitRepository() {
        Repository repository = new Repository("hitchhiker", "heartOfGold", "git");
        assertThat(SCMBuilderProvider.isSupported(repository)).isTrue();
    }

    @Test
    void shouldSupportMercurialRepository() {
        Repository repository = new Repository("hitchhiker", "heartOfGold", "hg");
        assertThat(SCMBuilderProvider.isSupported(repository)).isTrue();
    }

    @Test
    void shouldNotSupportUnknownRepository() {
        Repository repository = new Repository("hitchhiker", "heartOfGold", "bazar");
        assertThat(SCMBuilderProvider.isSupported(repository)).isFalse();
    }

    @Test
    void shouldCreateGitBuilder() {
        SCMBuilderProvider.Context context = context("git");
        SCMBuilder<?, ?> builder = SCMBuilderProvider.from(context);
        assertThat(builder).isInstanceOf(ScmManagerGitSCMBuilder.class);
    }

    @Test
    void shouldCreateMercurialBuilder() {
        SCMBuilderProvider.Context context = context("hg");
        SCMBuilder<?, ?> builder = SCMBuilderProvider.from(context);
        assertThat(builder).isInstanceOf(ScmManagerHgSCMBuilder.class);
    }

    @Test
    void shouldFailToCreateContextForUnknownType() {
        assertThrows(IllegalStateException.class, () -> SCMBuilderProvider.from(context("tsf")));
    }
}
