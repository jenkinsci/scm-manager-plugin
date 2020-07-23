package com.cloudogu.scmmanager;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.ning.http.client.AsyncHttpClient;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class ScmV2NotifierTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule();

  @Test
  public void testNotifyForChangesets() throws IOException, InterruptedException {
    testNotify(false, "/scm/api/v2/ci/ns/one/changesets/abc/jenkins/hitchhiker%2Fheart-of-gold");
  }

  @Test
  public void testNotifyForPullRequests() throws IOException, InterruptedException {
    testNotify(true, "/scm/api/v2/ci/ns/one/pullrequest/abc/jenkins/hitchhiker%2Fheart-of-gold");
  }

  private void testNotify(boolean pullRequest, String notificationUrl) throws IOException, InterruptedException {
    stubFor(
      put(notificationUrl)
        .willReturn(
          aResponse()
            .withStatus(200)
        )
    );

    URL instanceURL = createInstanceURL();
    NamespaceAndName namespaceAndName = new NamespaceAndName("ns", "one");

    ScmV2Notifier notifier =
      new ScmV2Notifier(
        instanceURL,
        namespaceAndName, req -> req.setHeader("Authenticated", "yes; awesome"),
        pullRequest);

    CountDownLatch cdl = new CountDownLatch(1);
    try (AsyncHttpClient client = new AsyncHttpClient()) {
      notifier.setClient(client);
      notifier.setCompletionListener((response -> cdl.countDown()));

      BuildStatus status = BuildStatus.success(
        "hitchhiker/heart-of-gold",
        "hitchhiker >> heart-of-gold",
        "https://hitchhiker.com"
      );

      notifier.notify("abc", status);

      cdl.await(30, TimeUnit.SECONDS);
    }

    verify(
      putRequestedFor(urlMatching(notificationUrl))
        .withHeader("Authenticated", equalTo("yes; awesome"))
        .withHeader("Content-Type", equalTo("application/vnd.scmm-cistatus+json;v=2"))
        .withRequestBody(
          matchingJsonPath("$.type", equalTo("jenkins"))
        )
        .withRequestBody(
          matchingJsonPath("$.name", equalTo("hitchhiker/heart-of-gold"))
        )
        .withRequestBody(
          matchingJsonPath("$.url", equalTo("https://hitchhiker.com"))
        )
        .withRequestBody(
          matchingJsonPath("$.status", equalTo("SUCCESS"))
        )
    );

  }

  private URL createInstanceURL() throws MalformedURLException {
    return new URL("http", "localhost", wireMockRule.port(), "/scm");
  }
}
