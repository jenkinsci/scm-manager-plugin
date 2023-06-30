package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.BearerHttpAuthentication;
import com.cloudogu.scmmanager.OkHttpClientBuilder;
import com.cloudogu.scmmanager.SSHAuthentication;
import com.cloudogu.scmmanager.SshConnection;
import com.cloudogu.scmmanager.SshConnectionFactory;
import com.cloudogu.scmmanager.SshConnectionFailedException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SshApiClient extends ApiClient {

  @VisibleForTesting
  static final String ACCESS_TOKEN_COMMAND = "scm-access-token";

  private static final Logger LOG = LoggerFactory.getLogger(SshApiClient.class);
  private static final String API_PATH = "/api/v2";

  // memoize access token, we only want a fresh access token if a new SshApiClient is created
  @SuppressWarnings("squid:4738") // jenkins provides an old guava version, so we can't use java.util.Supplier
  private final Supplier<AccessToken> fetcher = Suppliers.memoize(this::fetchAccessTokenFromSsh);

  private final OkHttpClient client;
  private final SshConnectionFactory connectionFactory;
  private final String sshUrl;
  private final SSHAuthentication authentication;

  public SshApiClient(String sshUrl, SSHAuthentication authentication) {
    this(OkHttpClientBuilder.build(), new SshConnectionFactory(), sshUrl, authentication);
  }

  public SshApiClient(OkHttpClient client, SshConnectionFactory connectionFactory, String sshUrl, SSHAuthentication authentication) {
    super("ssh");
    this.client = client;
    this.connectionFactory = connectionFactory;
    this.sshUrl = sshUrl;
    this.authentication = authentication;
  }

  @Override
  public <T> CompletableFuture<T> get(String url, String contentType, Class<T> type) {
    LOG.info("get {} from {}", type.getName(), url);

    // we can not use the supplier directly, the guava version which is provided by jenkins
    // does not yet implement the java.util.Supplier
    return CompletableFuture.supplyAsync(() -> fetcher.get())
      .thenCompose(token -> {
        String apiUrl = createApiUrl(token.getApiUrl(), url);

        Request.Builder requestBuilder = new Request.Builder().get().url(apiUrl);
        BearerHttpAuthentication.authenticate(requestBuilder, token.getAccessToken());
        requestBuilder.addHeader("Accept", contentType);

        return execute(client, requestBuilder, type);
      });
  }

  @Override
  public String getBaseUrl() {
    return createBaseUrl(fetcher.get().getApiUrl());
  }

  @VisibleForTesting
  static String createApiUrl(String apiUrl, String url) {
    if (url.startsWith("http")) {
      return url;
    }
    return createBaseUrl(apiUrl) + url;
  }

  private static String createBaseUrl(String apiUrl) {
    String baseUrl = apiUrl;
    if (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }

    if (!baseUrl.endsWith(API_PATH)) {
      throw new IllegalStateException("returned api url does not end with /api/v2");
    }

    return baseUrl.substring(0, baseUrl.length() - API_PATH.length());
  }

  private AccessToken fetchAccessTokenFromSsh() {
    LOG.info("connect to {} in order to fetch access token", sshUrl);
    try (SshConnection connection = createConnection()) {
      connection.connect(authentication);
      return executeTokenCommand(connection);
    }
  }

  private static AccessToken executeTokenCommand(SshConnection connection) {
    try {
      return connection.command(ACCESS_TOKEN_COMMAND)
        .withOutput(AccessToken.class)
        .json();
    } catch (IOException e) {
      throw new SshConnectionFailedException("failed to create ssh connection", e);
    }
  }

  private SshConnection createConnection() {
    return connectionFactory.create(sshUrl)
      .orElseThrow(() -> new IllegalStateException("could not create ssh connection"));
  }

  @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS") // we do not need equality checks
  static class AccessToken extends HalRepresentation {

    private String accessToken;

    public AccessToken() {
    }

    AccessToken(Links links, String accessToken) {
      super(links);
      this.accessToken = accessToken;
    }

    public String getAccessToken() {
      return accessToken;
    }

    public void setAccessToken(String accessToken) {
      this.accessToken = accessToken;
    }

    public String getApiUrl() {
      return getLinks().getLinkBy("index")
        .map(Link::getHref)
        .orElseThrow(() -> new IllegalStateException("no index link found"));
    }
  }
}
