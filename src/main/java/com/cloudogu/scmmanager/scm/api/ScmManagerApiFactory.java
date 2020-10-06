package com.cloudogu.scmmanager.scm.api;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudogu.scmmanager.BasicHttpAuthentication;
import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.SSHAuthentication;
import jenkins.scm.api.SCMSourceOwner;

public class ScmManagerApiFactory {

  private static final HttpAuthentication ANONYMOUS_AUTHENTICATION = requestBuilder -> {
  };

  public ScmManagerApi anonymous(String serverUrl) {
    return new ScmManagerApi(new HttpApiClient(serverUrl, ANONYMOUS_AUTHENTICATION));
  }

  public ScmManagerApi create(SCMSourceOwner owner, String serverUrl, String credentialsId) {
    StandardUsernamePasswordCredentials credentials = Credentials.findUsernamePassword(owner, serverUrl, credentialsId);
    return new ScmManagerApi(createApiClient(serverUrl, credentials));
  }

  private ApiClient createApiClient(String serverUrl, StandardUsernamePasswordCredentials credentials) {
    if (serverUrl.startsWith("http")) {
      return new HttpApiClient(serverUrl, BasicHttpAuthentication.from(credentials));
    } else if (serverUrl.startsWith("ssh")) {
      return new SshApiClient(serverUrl, SSHAuthentication.from(credentials));
    } else {
      throw new IllegalArgumentException("unsupported server url '" + serverUrl + "' only http or ssh urls are supported");
    }
  }

}
