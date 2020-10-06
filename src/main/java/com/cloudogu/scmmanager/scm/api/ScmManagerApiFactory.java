package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.HttpAuthentication;
import jenkins.scm.api.SCMSourceOwner;

public class ScmManagerApiFactory {

  public ScmManagerApi anonymous(String serverUrl) {
    return new ScmManagerApi(new HttpApiClient(serverUrl, requestBuilder -> {}));
  }

  public ScmManagerApi create(SCMSourceOwner owner, String serverUrl, String credentialsId) {
    HttpAuthentication authentication = Authentications.from(owner, serverUrl, credentialsId);
    return new ScmManagerApi(new HttpApiClient(serverUrl, authentication));
  }

}
