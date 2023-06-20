package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.JobInformation;
import hudson.model.Run;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmMigratedV1NotifierTest {

  private final MockWebServer server = new MockWebServer();

  @Mock
  private AuthenticationFactory authenticationFactory;

  @Mock
  private Run<?, ?> run;

  @Mock
  private ScmV2NotifierProvider v2NotifierProvider;

  @Before
  public void setUpServerAndClient() throws IOException {
    Dispatcher mDispatcher = new RecordedRequestDispatcher();
    server.setDispatcher(mDispatcher);
    server.start();
  }

  @Before
  public void prepareAuthentication() {
    when(authenticationFactory.createHttp(run, "one"))
      .thenReturn(response -> response.header("Auth", "Awesome"));
  }

  @Test
  public void testNotifyWithoutMatchingV2Location() throws InterruptedException, IOException {
    CountDownLatch cdl = new CountDownLatch(1);

    ScmMigratedV1Notifier notifier = createV1Notifier();
    AtomicReference<JobInformation> reference = applyV2Notifier(cdl, null);

    OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).build();
    notifier.setClient(client);
    notifier.notify("abc123", BuildStatus.success("old-repo", "Old-Repo", "https://oss.cloudogu.com"));

    cdl.await(30, TimeUnit.SECONDS);

    assertInfo(reference);
  }

  @Test
  public void testNotify() throws InterruptedException, IOException {
    CountDownLatch cdl = new CountDownLatch(1);

    ScmMigratedV1Notifier notifier = createV1Notifier();

    ScmV2Notifier v2Notifier = mock(ScmV2Notifier.class);
    AtomicReference<JobInformation> reference = applyV2Notifier(cdl, v2Notifier);

    BuildStatus success = BuildStatus.success("old-repo", "Old-Repo",  "https://oss.cloudogu.com");

    OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).build();
    notifier.setClient(client);
    notifier.notify("abc123", success);

    cdl.await(30, TimeUnit.SECONDS);

    assertInfo(reference);
    Mockito.verify(v2Notifier).notify("abc123", success);
  }

  private void assertInfo(AtomicReference<JobInformation> reference) {
    JobInformation received = reference.get();
    assertNotNull(received);
    assertEquals("git", received.getType());
    assertEquals("https://localhost/scm/old/repo", received.getUrl());
    assertEquals("abc123", received.getRevision());
    assertEquals("one", received.getCredentialsId());
  }

  private ScmMigratedV1Notifier createV1Notifier() {
    int port = server.getPort();
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
