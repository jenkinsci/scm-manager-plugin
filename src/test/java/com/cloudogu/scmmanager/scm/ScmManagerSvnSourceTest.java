package com.cloudogu.scmmanager.scm;

import static org.assertj.core.api.Assertions.assertThat;

import hudson.scm.SubversionSCM;
import jenkins.scm.api.SCMHead;
import org.junit.Test;

public class ScmManagerSvnSourceTest {

    @Test
    public void shouldReturnSubversionSCM() {
        ScmManagerSvnSource source = new ScmManagerSvnSource(
                "42", "https://hitchhiker.com/scm", "https://hitchhiker.com/scm/repo/spaceships/hog", "cred42");

        SubversionSCM scm = source.build(new SCMHead("trunk"), null);
        assertThat(scm).isInstanceOf(SubversionSCM.class);
        assertThat(scm.getBrowser()).isInstanceOf(ScmManagerSvnRepositoryBrowser.class);
    }
}
