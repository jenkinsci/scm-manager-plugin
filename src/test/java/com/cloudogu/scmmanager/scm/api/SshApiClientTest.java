package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.SSHAuthentication;
import com.cloudogu.scmmanager.SshConnection;
import com.cloudogu.scmmanager.SshConnectionFactory;
import com.cloudogu.scmmanager.SshConnectionFailedException;
import com.ning.http.client.AsyncHttpClient;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SshApiClientTest extends ApiClientTestBase {

  @Mock
  private SshConnectionFactory connectionFactory;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SshConnection connection;

  @Mock
  private SSHAuthentication authentication;

  @Test
  public void shouldExecute() throws IOException, ExecutionException, InterruptedException {
    when(connectionFactory.create("ssh://scm.hitchhiker.com")).thenReturn(Optional.of(connection));

    mockTokenResponse("ac-42");

    SshApiClient apiClient = new SshApiClient(getClient(), connectionFactory, "ssh://scm.hitchhiker.com", authentication);
    assertPong(apiClient);
  }

  private void assertPong(SshApiClient apiClient) throws InterruptedException, ExecutionException {
    CompletableFuture<Ping> future = apiClient.get("/api/v2/ping", "application/json", Ping.class);
    Ping ping = future.get();
    assertThat(ping.getResponse()).isEqualTo("pong");
  }

  @Test
  public void shouldCacheToken() throws IOException, ExecutionException, InterruptedException {
    when(connectionFactory.create("ssh://scm.hitchhiker.com")).thenReturn(Optional.of(connection));

    mockTokenResponse("ac-42", "ac-21");

    SshApiClient apiClient = new SshApiClient(getClient(), connectionFactory, "ssh://scm.hitchhiker.com", authentication);
    assertPong(apiClient);
    assertPong(apiClient);
    assertPong(apiClient);
  }

  @Test
  public void shouldReturnExceptionally() throws ExecutionException, InterruptedException {
    when(connectionFactory.create("ssh://scm.hitchhiker.com")).thenThrow(new SshConnectionFailedException("no conn"));
    SshApiClient apiClient = new SshApiClient(getClient(), connectionFactory, "ssh://scm.hitchhiker.com", authentication);
    CompletableFuture<Ping> future = apiClient.get("/api/v2/ping", "application/json", Ping.class);
    String result = future.thenApply(Ping::getResponse).exceptionally(Throwable::getMessage).get();
    assertThat(result).endsWith("no conn");
  }

  private void mockTokenResponse(String... bearer) throws IOException {
    AtomicInteger callCount = new AtomicInteger(-1);
    when(
      connection.command(SshApiClient.ACCESS_TOKEN_COMMAND).withOutput(SshApiClient.AccessToken.class).json()
    ).thenAnswer(ic -> createAccessToken(bearer[callCount.incrementAndGet()]));
  }

  private SshApiClient.AccessToken createAccessToken(String bearer) {
    String serverUrl = serverUrl("/api/v2");
    Links links = Links.linkingTo().single(Link.link("index", serverUrl)).build();
    return new SshApiClient.AccessToken(links, bearer);
  }

  @Test
  public void shouldCompleteReturnApiUrl() {
    String apiUrl = SshApiClient.createApiUrl("https://hitchhiker.com/scm/api/v2", "/api/v2/repositories");
    assertThat(apiUrl).isEqualTo("https://hitchhiker.com/scm/api/v2/repositories");
  }

  @Test
  public void shouldRemoveEndingSlashFromApiUrl() {
    String apiUrl = SshApiClient.createApiUrl("https://hitchhiker.com/scm/api/v2/", "/api/v2/repositories");
    assertThat(apiUrl).isEqualTo("https://hitchhiker.com/scm/api/v2/repositories");
  }

  @Test
  public void shouldNotModifyCompleteUrls() {
    String apiUrl = SshApiClient.createApiUrl("https://hitchhiker.com/scm/api/v2/", "https://scm.hitchhiker.com/scm/api/v2/repositories");
    assertThat(apiUrl).isEqualTo("https://scm.hitchhiker.com/scm/api/v2/repositories");
  }

  public static class Ping {

    private String response;

    public String getResponse() {
      return response;
    }

    public void setResponse(String response) {
      this.response = response;
    }
  }

}
