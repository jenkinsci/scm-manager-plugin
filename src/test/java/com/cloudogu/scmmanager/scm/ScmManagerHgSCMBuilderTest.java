package com.cloudogu.scmmanager.scm;

import static com.cloudogu.scmmanager.scm.ScmManagerHgSCMBuilder.findAndCreateBrowser;
import static org.assertj.core.api.Assertions.assertThat;

import hudson.plugins.mercurial.browser.HgBrowser;
import hudson.plugins.mercurial.browser.ScmManager;
import org.junit.jupiter.api.Test;

class ScmManagerHgSCMBuilderTest {

    @Test
    void shouldReturnNullIfBrowserDoesNotExists() {
        HgBrowser browser = findAndCreateBrowser("UnknownBrowser", "https://hitchhiker.com");
        assertThat(browser).isNull();
    }

    @Test
    void shouldReturnForInvalidBrowser() {
        HgBrowser browser = findAndCreateBrowser("java.lang.Integer", "https://hitchhiker.com");
        assertThat(browser).isNull();
    }

    @Test
    void shouldReturnScmManagerBrowser() {
        HgBrowser browser = findAndCreateBrowser("https://hitchhiker.com");
        assertThat(browser).isInstanceOf(ScmManager.class);
    }
}
