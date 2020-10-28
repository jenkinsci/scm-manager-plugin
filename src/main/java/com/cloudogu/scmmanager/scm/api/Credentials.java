package com.cloudogu.scmmanager.scm.api;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.Queue;
import hudson.security.ACL;
import jenkins.scm.api.SCMSourceOwner;

class Credentials {

  private Credentials() {
  }

  public static StandardUsernameCredentials findSshCredentials(SCMSourceOwner owner, String serverUrl, String credentialsId) {
    return findCredentials(StandardUsernameCredentials.class, owner, serverUrl, credentialsId);
  }

  public static StandardUsernamePasswordCredentials findHttpCredentials(SCMSourceOwner owner, String serverUrl, String credentialsId) {
    return findCredentials(StandardUsernamePasswordCredentials.class, owner, serverUrl, credentialsId);
  }

  @Nullable
  private static <C extends com.cloudbees.plugins.credentials.Credentials> C findCredentials(Class<C> credentialsType, SCMSourceOwner owner, String serverUrl, String credentialsId) {
    return CredentialsMatchers.firstOrNull(
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
  }

}
