package com.cloudogu.scmmanager;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import hudson.model.Run;

import java.util.Collections;

class AuthenticationFactory {

  @VisibleForTesting
  static final Authentication NOOP_AUTHENTICATION = requestBuilder -> {};

  Authentication create(Run<?, ?> run, String credentialsId) {
    if (Strings.isNullOrEmpty(credentialsId)) {
      return NOOP_AUTHENTICATION;
    }
    StandardUsernamePasswordCredentials credentials = CredentialsProvider.findCredentialById(credentialsId, StandardUsernamePasswordCredentials.class, run, Collections.emptyList());
    if (credentials == null) {
      return NOOP_AUTHENTICATION;
    }

    return new BasicAuthentication(credentials.getUsername(), credentials.getPassword());
  }

}
