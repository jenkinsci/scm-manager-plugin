package com.cloudogu.scmmanager.scm;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.List;
import jenkins.model.Jenkins;
import okhttp3.mockwebserver.MockWebServer;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebRequest;
import org.htmlunit.util.NameValuePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class DescriptorEndpointSecurityTest {

    private static final String LOW_PRIVILEGE_USER = "lowpriv";
    private static final String NAVIGATOR_DESCRIPTOR =
            "descriptorByName/com.cloudogu.scmmanager.scm.ScmManagerNavigator";
    private static final String SOURCE_DESCRIPTOR =
            "descriptorByName/com.cloudogu.scmmanager.scm.ScmManagerSource";

    private JenkinsRule j;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        j = rule;
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                .grant(Jenkins.READ)
                .everywhere()
                .to(LOW_PRIVILEGE_USER)
                .grant(Jenkins.ADMINISTER)
                .everywhere()
                .to("admin"));
    }

    @Test
    void shouldRequirePostForServerUrlChecks() throws Exception {
        assertGetIsRejected(NAVIGATOR_DESCRIPTOR + "/checkServerUrl?value=http://example.com");
        assertGetIsRejected(SOURCE_DESCRIPTOR + "/checkServerUrl?value=http://example.com");
    }

    @Test
    void shouldSkipLowPrivilegeCredentialChecksBeforeConnectingToServerUrl() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.start();
            String serverUrl = server.url("/").toString();

            assertPostIsOk(
                    NAVIGATOR_DESCRIPTOR + "/checkCredentialsId",
                    List.of(new NameValuePair("serverUrl", serverUrl), new NameValuePair("value", "scm-creds")));
            assertPostIsOk(
                    SOURCE_DESCRIPTOR + "/checkCredentialsId",
                    List.of(new NameValuePair("serverUrl", serverUrl), new NameValuePair("value", "scm-creds")));

            assertThat(server.getRequestCount()).isZero();
        }
    }

    private void assertGetIsRejected(String path) throws Exception {
        JenkinsRule.WebClient webClient = lowPrivilegeWebClient();

        Page page = webClient.goTo(path, "text/html");

        assertThat(page.getWebResponse().getStatusCode()).isEqualTo(405);
    }

    private void assertPostIsOk(String path, List<NameValuePair> parameters) throws Exception {
        JenkinsRule.WebClient webClient = lowPrivilegeWebClient();
        WebRequest request = new WebRequest(new URL(j.getURL(), path), HttpMethod.POST);
        request.setRequestParameters(parameters);

        Page page = webClient.getPage(webClient.addCrumb(request));

        assertThat(page.getWebResponse().getStatusCode()).isEqualTo(200);
    }

    private JenkinsRule.WebClient lowPrivilegeWebClient() {
        return j.createWebClient()
                .withBasicCredentials(LOW_PRIVILEGE_USER, LOW_PRIVILEGE_USER)
                .withThrowExceptionOnFailingStatusCode(false);
    }
}
