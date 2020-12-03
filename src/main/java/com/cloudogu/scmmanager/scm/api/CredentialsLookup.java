package com.cloudogu.scmmanager.scm.api;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Queue;
import hudson.security.ACL;

import java.util.List;

class CredentialsLookup {

  CredentialsLookup() {
  }

  public Lookup<StandardUsernameCredentials> ssh(String serverUrl, String credentialsId) {
    if (!serverUrl.startsWith("ssh")) {
      throw new IllegalArgumentException(String.format("ssh url is required, receive %s instead", serverUrl));
    }
    return new Lookup<>(StandardUsernameCredentials.class, serverUrl, credentialsId);
  }

  public Lookup<StandardUsernamePasswordCredentials> http(String serverUrl, String credentialsId) {
    if (!serverUrl.startsWith("http")) {
      throw new IllegalArgumentException(String.format("http url is required, received %s instead", serverUrl));
    }
    return new Lookup<>(StandardUsernamePasswordCredentials.class, serverUrl, credentialsId);
  }

  public static final class Lookup<C extends com.cloudbees.plugins.credentials.Credentials> {

    private final Class<C> type;
    private final String serverUrl;
    private final String credentialsId;

    private Lookup(Class<C> type, String serverUrl, String credentialsId) {
      this.type = type;
      this.serverUrl = serverUrl;
      this.credentialsId = credentialsId;
    }

    public C lookup(Item item) {
      List<C> credentialList = CredentialsProvider.lookupCredentials(
        type,
        item,
        item instanceof Queue.Task
          ? ((Queue.Task) item).getDefaultAuthentication()
          : ACL.SYSTEM,
        URIRequirementBuilder.fromUri(serverUrl).build()
      );
      return find(credentialList);
    }

    public C lookup(ItemGroup<?> itemGroup) {
      List<C> credentialList = CredentialsProvider.lookupCredentials(
        type,
        itemGroup,
        ACL.SYSTEM,
        URIRequirementBuilder.fromUri(serverUrl).build()
      );
      return find(credentialList);
    }

    private C find(List<C> credentialList) {
      C credentials = CredentialsMatchers.firstOrNull(
        credentialList,
        CredentialsMatchers.allOf(
          CredentialsMatchers.withId(credentialsId),
          CredentialsMatchers.anyOf(CredentialsMatchers.instanceOf(type))
        )
      );

      if (credentials == null) {
        throw new CredentialsUnavailableException(String.format("could not find credentials %s of type %s", credentialsId, type));
      }

      return credentials;
    }

  }
}
