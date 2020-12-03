package com.cloudogu.scmmanager.scm.api;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudogu.scmmanager.BasicHttpAuthentication;
import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.SSHAuthentication;
import com.google.common.annotations.VisibleForTesting;
import hudson.model.Item;
import hudson.model.ItemGroup;

public class ScmManagerApiFactory {

  private static final HttpAuthentication ANONYMOUS_AUTHENTICATION = requestBuilder -> {
  };

  private final CredentialsLookup credentialsLookup;

  public ScmManagerApiFactory() {
    this(new CredentialsLookup());
  }

  @VisibleForTesting
  ScmManagerApiFactory(CredentialsLookup credentialsLookup) {
    this.credentialsLookup = credentialsLookup;
  }

  public ScmManagerApi anonymous(String serverUrl) {
    return new ScmManagerApi(new HttpApiClient(serverUrl, ANONYMOUS_AUTHENTICATION));
  }

  public ScmManagerApi create(Item item, String serverUrl, String credentialsId) {
    return new ScmManagerApi(apiClientFactory(serverUrl, credentialsId).create(item));
  }

  public ScmManagerApi create(ItemGroup<?> itemGroup, String serverUrl, String credentialsId) {
    return new ScmManagerApi(apiClientFactory(serverUrl, credentialsId).create(itemGroup));
  }

  private ApiClientFactory apiClientFactory(String serverUrl, String credentialsId) {
    if (serverUrl.startsWith("http")) {
      return new HttpApiClientFactory(serverUrl, credentialsId);
    } else if (serverUrl.startsWith("ssh")) {
      return new SshApiClientFactory(serverUrl, credentialsId);
    } else {
      throw new IllegalArgumentException("unsupported server url '" + serverUrl + "' only http or ssh urls are supported");
    }
  }

  private interface ApiClientFactory {

    ApiClient create(Item item);

    ApiClient create(ItemGroup<?> item);

  }

  private class HttpApiClientFactory implements ApiClientFactory {

    private final String serverUrl;
    private final CredentialsLookup.Lookup<StandardUsernamePasswordCredentials> credentials;

    public HttpApiClientFactory(String serverUrl, String credentialsId) {
      this.serverUrl = serverUrl;
      credentials = credentialsLookup.http(serverUrl, credentialsId);
    }

    @Override
    public ApiClient create(Item item) {
      return create(credentials.lookup(item));
    }

    @Override
    public ApiClient create(ItemGroup<?> item) {
      return create(credentials.lookup(item));
    }

    private ApiClient create(StandardUsernamePasswordCredentials credentials) {
      return new HttpApiClient(serverUrl, BasicHttpAuthentication.from(credentials));
    }
  }

  private class SshApiClientFactory implements ApiClientFactory {

    private final String serverUrl;
    private final CredentialsLookup.Lookup<StandardUsernameCredentials> credentials;

    public SshApiClientFactory(String serverUrl, String credentialsId) {
      this.serverUrl = serverUrl;
      credentials = credentialsLookup.ssh(serverUrl, credentialsId);
    }

    @Override
    public ApiClient create(Item item) {
      return create(credentials.lookup(item));
    }

    @Override
    public ApiClient create(ItemGroup<?> item) {
      return create(credentials.lookup(item));
    }

    private ApiClient create(StandardUsernameCredentials credentials) {
      return new SshApiClient(serverUrl, SSHAuthentication.from(credentials));
    }
  }

}
