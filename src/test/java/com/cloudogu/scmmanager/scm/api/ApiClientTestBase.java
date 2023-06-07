package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.HttpAuthentication;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import okhttp3.OkHttpClient;
import org.junit.Before;
import org.junit.Rule;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class ApiClientTestBase {
  @Rule
  public WireMockRule rule = new WireMockRule(options().dynamicPort());

  private OkHttpClient client;

  private String[] pathInjection = {};
  private int pathInjectionIndex = 0;

  @Before
  public void setUpAHC() {
    this.client = new OkHttpClient();
  }

  public OkHttpClient getClient() {
    return client;
  }

  protected ApiClient apiClient() {
    HttpAuthentication noAuthentication = requestBuilder -> {};
    return new HttpApiClient(client, noAuthentication, this::serverUrl);
  }

  protected void injectPath(String... pathInjection) {
    this.pathInjection = pathInjection;
  }

  protected String serverUrl(String path) {
    return String.format("http://localhost:%d%s", rule.port(), nextPathInjection() + path);
  }

  private String nextPathInjection() {
    if (pathInjection.length == 0) {
      return "";
    }
    if (pathInjection.length == 1) {
      return pathInjection[0];
    }
    return pathInjection[pathInjectionIndex++];
  }
}
