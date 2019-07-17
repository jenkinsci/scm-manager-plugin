package com.cloudogu.scmmanager;

import com.google.common.annotations.VisibleForTesting;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import jenkins.plugins.asynchttpclient.AHC;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public final class ScmV2Notifier implements Notifier {

  private static final Logger LOG = LoggerFactory.getLogger(ScmV2Notifier.class);

  private static final String URL = "%s/api/v2/ci/%s/%s/changesets/%s/%s/%s";

  private final URL instance;
  private final NamespaceAndName namespaceAndName;
  private final Authentication authentication;

  private AsyncHttpClient client;

  @VisibleForTesting
  private Consumer<Response> completionListener = response -> {};

  ScmV2Notifier(URL instance, NamespaceAndName namespaceAndName, Authentication authentication) {
    this.instance = instance;
    this.namespaceAndName = namespaceAndName;
    this.authentication = authentication;
  }

  @VisibleForTesting
  URL getInstance() {
    return instance;
  }

  @VisibleForTesting
  NamespaceAndName getNamespaceAndName() {
    return namespaceAndName;
  }

  @VisibleForTesting
  Authentication getAuthentication() {
    return authentication;
  }

  @VisibleForTesting
  void setClient(AsyncHttpClient client) {
    this.client = client;
  }

  @VisibleForTesting
  void setCompletionListener(Consumer<Response> completionListener) {
    this.completionListener = completionListener;
  }

  public AsyncHttpClient getClient() {
    if (client == null) {
      return AHC.instance();
    }
    return client;
  }

  @Override
  public void notify(String revision, BuildStatus buildStatus) {
    LOG.info("set rev {} of {} to {}", revision, namespaceAndName, buildStatus.getStatus());

    String url = createUrl(revision, buildStatus);
    LOG.info("send build status to {}", url);

    AsyncHttpClient.BoundRequestBuilder post = getClient().preparePost(url);
    authentication.authenticate(post);

    post.setHeader("Content-Type", "application/json; charset=UTF-8")
      .setBody(createRequestBody(buildStatus))
      .execute(new AsyncCompletionHandler<Object>() {
        @Override
        public void onThrowable(Throwable t) {
          LOG.warn("failed to notify scm-manager", t);
        }

        @Override
        public Object onCompleted(Response response) {
          LOG.info(
            "status notify for repository {} and revision {} returned {}",
            namespaceAndName, revision, response.getStatusCode()
          );
          completionListener.accept(response);
          return null;
        }
      });
  }

  private byte[] createRequestBody(BuildStatus buildStatus) {
    JSONObject jsonObject = JSONObject.fromObject(buildStatus);
    return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
  }

  private String createUrl(String revision, BuildStatus buildStatus) {
    return String.format(URL,
      instance.toExternalForm(),
      namespaceAndName.getNamespace(),
      namespaceAndName.getName(),
      revision,
      buildStatus.getType(),
      buildStatus.getName()
    );
  }
}