package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.JobInformation;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.ning.http.client.AsyncHttpClient;
import hudson.model.Run;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmMigratedV1NotifierTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule();

  @Mock
  private AuthenticationFactory authenticationFactory;

  @Mock
  private Run<?, ?> run;

  @Mock
  private ScmV2NotifierProvider v2NotifierProvider;

  @Before
  public void prepareAuthentication() {
    when(authenticationFactory.createHttp(run, "one"))
      .thenReturn(response -> response.setHeader("Auth", "Awesome"));
  }

  @Test
  public void testNotifyWithoutMatchingV2Location() throws InterruptedException, MalformedURLException {
    stubResource();

    CountDownLatch cdl = new CountDownLatch(1);

    ScmMigratedV1Notifier notifier = createV1Notifier();
    AtomicReference<JobInformation> reference = applyV2Notifier(cdl, null);

    try (AsyncHttpClient client = new AsyncHttpClient()) {
      notifier.setClient(client);
      notifier.notify("abc123", BuildStatus.success("old-repo", "Old-Repo", "https://oss.cloudogu.com"));

      cdl.await(30, TimeUnit.SECONDS);
    }

    assertInfo(reference);
  }

  @Test
  public void testNotify() throws InterruptedException, IOException {
    stubResource();

    CountDownLatch cdl = new CountDownLatch(1);

    ScmMigratedV1Notifier notifier = createV1Notifier();

    ScmV2Notifier v2Notifier = mock(ScmV2Notifier.class);
    AtomicReference<JobInformation> reference = applyV2Notifier(cdl, v2Notifier);

    BuildStatus success = BuildStatus.success("old-repo", "Old-Repo",  "https://oss.cloudogu.com");

    try (AsyncHttpClient client = new AsyncHttpClient()) {
      notifier.setClient(client);
      notifier.notify("abc123", success);

      cdl.await(30, TimeUnit.SECONDS);
    }

    assertInfo(reference);
    Mockito.verify(v2Notifier).notify("abc123", success);
  }

  private void stubResource() {
    stubFor(
      get("/scm/git/some/old/repo")
        .withHeader("Auth", equalTo("Awesome"))
        .willReturn(
          aResponse()
            .withHeader("Location", "https://scm.scm-manager.org/scm/old/repo")
            .withStatus(301)
        )
    );
  }

  private void assertInfo(AtomicReference<JobInformation> reference) {
    JobInformation received = reference.get();
    assertEquals("git", received.getType());
    assertEquals("https://scm.scm-manager.org/scm/old/repo", received.getUrl());
    assertEquals("abc123", received.getRevision());
    assertEquals("one", received.getCredentialsId());
  }

  private ScmMigratedV1Notifier createV1Notifier() {
    int port = wireMockRule.port();
    String url = String.format("http://localhost:%d/scm/git/some/old/repo", port);

    JobInformation information = new JobInformation("git", url, "abc", "one", false);
    ScmMigratedV1Notifier v1Notifier = new ScmMigratedV1Notifier(authenticationFactory, run, information);
    v1Notifier.setV2NotifierProvider(v2NotifierProvider);
    return v1Notifier;
  }

  private AtomicReference<JobInformation> applyV2Notifier(CountDownLatch cdl, ScmV2Notifier notifier) throws MalformedURLException {
    AtomicReference<JobInformation> reference = new AtomicReference<>();
    when(v2NotifierProvider.get(Mockito.any(Run.class), Mockito.any(JobInformation.class))).then(ic -> {
      cdl.countDown();
      reference.set(ic.getArgument(1));
      return Optional.ofNullable(notifier);
    });
    return reference;
  }

}
