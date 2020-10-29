package com.cloudogu.scmmanager.scm.api;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudogu.scmmanager.BasicHttpAuthentication;
import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.SSHAuthentication;
import com.google.common.annotations.VisibleForTesting;
import jenkins.scm.api.SCMSourceOwner;

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

  public ScmManagerApi create(SCMSourceOwner owner, String serverUrl, String credentialsId) {
    return new ScmManagerApi(createApiClient(owner, serverUrl, credentialsId));
  }

  private ApiClient createApiClient(SCMSourceOwner owner, String serverUrl, String credentialsId) {
    if (serverUrl.startsWith("http")) {
      return createHttpApiClient(owner, serverUrl, credentialsId);
    } else if (serverUrl.startsWith("ssh")) {
      return createSshApiClient(owner, serverUrl, credentialsId);
    } else {
      throw new IllegalArgumentException("unsupported server url '" + serverUrl + "' only http or ssh urls are supported");
    }
  }

  private ApiClient createHttpApiClient(SCMSourceOwner owner, String serverUrl, String credentialsId) {
    StandardUsernamePasswordCredentials credentials = credentialsLookup.http(owner, serverUrl, credentialsId);
    return new HttpApiClient(serverUrl, BasicHttpAuthentication.from(credentials));
  }

  private ApiClient createSshApiClient(SCMSourceOwner owner, String serverUrl, String credentialsId) {
    StandardUsernameCredentials credentials = credentialsLookup.ssh(owner, serverUrl, credentialsId);
    return new SshApiClient(serverUrl, SSHAuthentication.from(credentials));
  }

}
