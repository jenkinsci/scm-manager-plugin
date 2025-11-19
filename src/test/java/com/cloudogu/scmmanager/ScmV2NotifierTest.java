package com.cloudogu.scmmanager;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

class ScmV2NotifierTest {

    private final MockWebServer server = new MockWebServer();

    @Test
    void testNotifyForChangesets() throws IOException, InterruptedException {
        testNotify(
                "/scm/api/v2/ci/ns/one/changesets/abc/jenkins/hitchhiker%2Fheart-of-gold",
                "hitchhiker/heart-of-gold", null);

        RecordedRequest request = server.takeRequest();
        assertThat(request.getRequestUrl().encodedPath())
                .isEqualTo("/scm/api/v2/ci/ns/one/changesets/abc/jenkins/hitchhiker%2Fheart-of-gold");
        assertThat(request.getHeader("Authenticated")).isEqualTo("yes; awesome");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/vnd.scmm-cistatus+json;v=2");
        JsonObject jsonElement =
                JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
        assertThat(jsonElement.get("type").getAsString()).isEqualTo("jenkins");
        assertThat(jsonElement.get("name").getAsString()).isEqualTo("hitchhiker/heart-of-gold");
        assertThat(jsonElement.get("url").getAsString()).isEqualTo("https://hitchhiker.com");
        assertThat(jsonElement.get("status").getAsString()).isEqualTo("SUCCESS");
    }

    @Test
    void testNotifyForPullRequests() throws IOException, InterruptedException {
        testNotify(
                "/scm/api/v2/ci/ns/one/pullrequest/abc/jenkins/hitchhiker%2Fpr-1",
                "hitchhiker/heart-of-gold", "hitchhiker/pr-1");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getRequestUrl().encodedPath())
                .isEqualTo("/scm/api/v2/ci/ns/one/pullrequest/abc/jenkins/hitchhiker%2Fpr-1");
        assertThat(request.getHeader("Authenticated")).isEqualTo("yes; awesome");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/vnd.scmm-cistatus+json;v=2");
        JsonObject jsonElement =
                JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
        assertThat(jsonElement.get("type").getAsString()).isEqualTo("jenkins");
        assertThat(jsonElement.get("name").getAsString()).isEqualTo("hitchhiker/pr-1");
        assertThat(jsonElement.get("url").getAsString()).isEqualTo("https://hitchhiker.com");
        assertThat(jsonElement.get("status").getAsString()).isEqualTo("SUCCESS");
        assertThat(jsonElement.get("replaces").getAsString()).isEqualTo("hitchhiker/hitchhiker%2Fheart-of-gold");
    }

    private void testNotify(String notificationUrl, String branch, String pullRequest)
            throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(200));
        server.start();

        URL instanceURL = createInstanceURL();
        NamespaceAndName namespaceAndName = new NamespaceAndName("ns", "one");

        ScmV2Notifier notifier = new ScmV2Notifier(
                instanceURL,
                namespaceAndName,
                req -> req.header("Authenticated", "yes; awesome"),
                pullRequest != null,
                pullRequest == null ? null : branch);

        CountDownLatch cdl = new CountDownLatch(1);
        OkHttpClient client = new OkHttpClient();
        notifier.setClient(client);
        notifier.setCompletionListener((response -> cdl.countDown()));

        BuildStatus status = BuildStatus.success(
                pullRequest != null ? pullRequest : branch, "hitchhiker >> heart-of-gold", "https://hitchhiker.com");

        notifier.notify("abc", status);

        cdl.await(30, TimeUnit.SECONDS);
    }

    private URL createInstanceURL() throws MalformedURLException {
        return new URL("http", "localhost", server.getPort(), "/scm");
    }
}
