package com.cloudogu.scmmanager.scm.api;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudogu.scmmanager.BasicHttpAuthentication;
import com.cloudogu.scmmanager.HttpAuthentication;
import hudson.model.Queue;
import hudson.security.ACL;
import jenkins.scm.api.SCMSourceOwner;

public class Authentications {

  private SCMSourceOwner owner;

  public static HttpAuthentication from(SCMSourceOwner owner, String serverUrl, String credentialsId) {
    return new Authentications(owner).from(serverUrl, credentialsId);
  }

  public Authentications(SCMSourceOwner owner) {
    this.owner = owner;
  }

  public HttpAuthentication from(String serverUrl, String credentialsId) {
    StandardUsernamePasswordCredentials credentials = credentials(serverUrl, credentialsId);
    return BasicHttpAuthentication.from(credentials);
  }

  private StandardUsernamePasswordCredentials credentials(String serverUrl, String credentialsId) {
    return CredentialsMatchers.firstOrNull(
      CredentialsProvider.lookupCredentials(
        StandardUsernamePasswordCredentials.class,
        owner,
        owner instanceof Queue.Task
          ? ((Queue.Task) owner).getDefaultAuthentication()
          : ACL.SYSTEM,
        URIRequirementBuilder.fromUri(serverUrl).build()
      ),
      CredentialsMatchers.allOf(
        CredentialsMatchers.withId(credentialsId),
        CredentialsMatchers.anyOf(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class))
      )
    );
  }
}
