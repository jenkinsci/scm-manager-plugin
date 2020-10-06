package com.cloudogu.scmmanager.scm.api;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.model.Queue;
import hudson.security.ACL;
import jenkins.scm.api.SCMSourceOwner;

class Credentials {

  private Credentials() {
  }

  public static StandardUsernamePasswordCredentials findUsernamePassword(SCMSourceOwner owner, String serverUrl, String credentialsId) {
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
