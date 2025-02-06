package com.cloudogu.scmmanager.scm;

import hudson.plugins.mercurial.browser.HgBrowser;
import hudson.plugins.mercurial.browser.ScmManager;
import org.junit.Test;

import static com.cloudogu.scmmanager.scm.ScmManagerHgSCMBuilder.findAndCreateBrowser;
import static org.assertj.core.api.Assertions.assertThat;

public class ScmManagerHgSCMBuilderTest {

    @Test
    public void shouldReturnNullIfBrowserDoesNotExists() {
        HgBrowser browser = findAndCreateBrowser("UnknownBrowser", "https://hitchhiker.com");
        assertThat(browser).isNull();
    }

    @Test
    public void shouldReturnForInvalidBrowser() {
        HgBrowser browser = findAndCreateBrowser("java.lang.Integer", "https://hitchhiker.com");
        assertThat(browser).isNull();
    }

    @Test
    public void shouldReturnScmManagerBrowser() {
        HgBrowser browser = findAndCreateBrowser("https://hitchhiker.com");
        assertThat(browser).isInstanceOf(ScmManager.class);
    }
}
