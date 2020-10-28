package com.cloudogu.scmmanager.scm.api;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.google.common.base.Preconditions;
import hudson.model.Queue;
import hudson.security.ACL;
import jenkins.scm.api.SCMSourceOwner;

class CredentialsLookup {

  CredentialsLookup() {
  }

  public StandardUsernameCredentials ssh(SCMSourceOwner owner, String serverUrl, String credentialsId) {
    Preconditions.checkArgument(serverUrl.startsWith("ssh"), "ssh url is required, receive %s instead", serverUrl);
    return lookup(StandardUsernameCredentials.class, owner, serverUrl, credentialsId);
  }

  public StandardUsernamePasswordCredentials http(SCMSourceOwner owner, String serverUrl, String credentialsId) {
    Preconditions.checkArgument(serverUrl.startsWith("http"), "http url is required, receive %s instead", serverUrl);
    return lookup(StandardUsernamePasswordCredentials.class, owner, serverUrl, credentialsId);
  }

  private static <C extends com.cloudbees.plugins.credentials.Credentials> C lookup(Class<C> credentialsType, SCMSourceOwner owner, String serverUrl, String credentialsId) {
    C credentials = CredentialsMatchers.firstOrNull(
      CredentialsProvider.lookupCredentials(
        credentialsType,
        owner,
        owner instanceof Queue.Task
          ? ((Queue.Task) owner).getDefaultAuthentication()
          : ACL.SYSTEM,
        URIRequirementBuilder.fromUri(serverUrl).build()
      ),
      CredentialsMatchers.allOf(
        CredentialsMatchers.withId(credentialsId),
        CredentialsMatchers.anyOf(CredentialsMatchers.instanceOf(credentialsType))
      )
    );

    if (credentials == null) {
      throw new CredentialsUnavailableException(String.format("could not find credentials %s of type %s", credentialsId, credentialsType));
    }

    return credentials;
  }

}
