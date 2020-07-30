package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import jenkins.scm.api.trait.SCMBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.assertj.core.api.Assertions.assertThat;

public class SCMBuilderProviderTest {

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();

  @Test
  public void shouldSupportGitRepository() {
    Repository repository = new Repository("hitchhiker", "heartOfGold", "git");
    assertThat(SCMBuilderProvider.isSupported(repository)).isTrue();
  }

  @Test
  public void shouldNotSupportUnknownRepository() {
    Repository repository = new Repository("hitchhiker", "heartOfGold", "bazar");
    assertThat(SCMBuilderProvider.isSupported(repository)).isFalse();
  }

  @Test
  public void shouldCreateGitBuilder() {
    SCMBuilderProvider.Context context = createContext("git");
    SCMBuilder<?, ?> builder = SCMBuilderProvider.from(context);
    assertThat(builder).isInstanceOf(ScmManagerGitSCMBuilder.class);
  }

  private SCMBuilderProvider.Context createContext(String type) {
    CloneInformation cloneInformation = new CloneInformation(type, "https://scm-manager.org/repos/hitchhiker/heartOfGold");
    ScmManagerHead develop = new ScmManagerHead(cloneInformation, "develop");
    return new SCMBuilderProvider.Context(
      develop,
      new ScmManagerRevision(develop, "f572d396fae9206628714fb2ce00f72e94f2258f"),
      "creds4scm"
    );
  }
}
