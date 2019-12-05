package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.ScmInformation;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.jcraft.jsch.JSchException;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import hudson.model.Run;
import jenkins.plugins.asynchttpclient.AHC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class ScmMigratedV1Notifier implements Notifier {

  private static final Logger LOG = LoggerFactory.getLogger(ScmMigratedV1Notifier.class);
  private final AuthenticationFactory authenticationFactory;
  private final Run<?, ?> run;
  private final ScmInformation information;

  private AsyncHttpClient client;
  private ScmV2NotifierProvider v2NotifierProvider;

  ScmMigratedV1Notifier(AuthenticationFactory authenticationFactory, Run<?, ?> run, ScmInformation information) {
    this.authenticationFactory = authenticationFactory;
    this.run = run;
    this.information = information;
  }

  @VisibleForTesting
  void setClient(AsyncHttpClient client) {
    this.client = client;
  }

  private AsyncHttpClient getClient() {
    if (client != null) {
      return client;
    }
    return AHC.instance();
  }

  @VisibleForTesting
  void setV2NotifierProvider(ScmV2NotifierProvider v2NotifierProvider) {
    this.v2NotifierProvider = v2NotifierProvider;
  }

  private ScmV2NotifierProvider getV2NotifierProvider() {
    if (v2NotifierProvider == null) {
      return new ScmV2NotifierProvider();
    }
    return v2NotifierProvider;
  }

  @Override
  public void notify(String revision, BuildStatus buildStatus) {
    AsyncHttpClient.BoundRequestBuilder request = getClient().prepareGet(information.getUrl())
      .setFollowRedirects(false);
    authenticationFactory.create(run, information.getCredentialsId()).authenticate(request);
    request.execute(new AsyncCompletionHandler<Object>() {

      @Override
      public void onThrowable(Throwable t) {
        LOG.warn("failed to get redirect uri from migrated repository", t);
      }

      @Override
      public Object onCompleted(Response response) throws Exception {
        if (response.isRedirected()) {
          String location = response.getHeader("Location");
          if (!Strings.isNullOrEmpty(location)) {
            notifyV2(location, revision, buildStatus);
          } else {
            LOG.warn("server returned redirect without location header");
          }
        } else {
          LOG.debug("expected redirect, but server returned status code {}", response.getStatusCode());
        }
        return null;
      }
    });
  }

  private void notifyV2(String location, String revision, BuildStatus buildStatus) throws IOException, JSchException {
    ScmV2NotifierProvider provider = getV2NotifierProvider();
    provider.setAuthenticationFactory(authenticationFactory);

    ScmInformation redirectedInformation = new ScmInformation(
      information.getType(),
      location,
      revision,
      information.getCredentialsId()
    );

    Optional<ScmV2Notifier> scmV2Notifier = provider.get(run, redirectedInformation);
    if (scmV2Notifier.isPresent()) {
      LOG.debug("notify v2 url {}", location);
      scmV2Notifier.get().notify(revision, buildStatus);
    } else {
      LOG.debug("redirect uri {} does not look like a scm v2 repo url", location);
    }
  }
}
