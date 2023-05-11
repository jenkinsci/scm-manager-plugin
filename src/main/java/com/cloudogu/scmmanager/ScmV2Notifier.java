package com.cloudogu.scmmanager;

import com.google.common.annotations.VisibleForTesting;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import jenkins.plugins.asynchttpclient.AHC;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ScmV2Notifier implements Notifier {

  private static final Logger LOG = LoggerFactory.getLogger(ScmV2Notifier.class);

  private static final String CHANGESET_URL = "%s/api/v2/ci/%s/%s/changesets/%s/%s/%s";
  private static final String PULL_REQUEST_URL = "%s/api/v2/ci/%s/%s/pullrequest/%s/%s/%s";

  private final URL instance;
  private final NamespaceAndName namespaceAndName;
  private final HttpAuthentication httpAuthentication;
  private final boolean pullRequest;
  private final String sourceBranch;

  private AsyncHttpClient client;

  private Consumer<Response> completionListener = response -> {
  };

  ScmV2Notifier(URL instance, NamespaceAndName namespaceAndName, HttpAuthentication httpAuthentication, boolean pullRequest, String sourceBranch) {
    this.instance = instance;
    this.namespaceAndName = namespaceAndName;
    this.httpAuthentication = httpAuthentication;
    this.pullRequest = pullRequest;
    this.sourceBranch = sourceBranch;
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
  HttpAuthentication getHttpAuthentication() {
    return httpAuthentication;
  }

  public String getSourceBranch() {
    return sourceBranch;
  }

  @VisibleForTesting
  void setClient(AsyncHttpClient client) {
    this.client = client;
  }

  @VisibleForTesting
  void setCompletionListener(Consumer<Response> completionListener) {
    this.completionListener = completionListener;
  }

  private AsyncHttpClient getClient() {
    if (client == null) {
      return AHC.instance();
    }
    return client;
  }

  @Override
  public void notify(String revision, BuildStatus buildStatus) throws IOException {
    LOG.info("set rev {} of {} to {}", revision, namespaceAndName, buildStatus.getStatus());

    String url = createUrl(revision, buildStatus);
    LOG.info("send build status to {}", url);

    AsyncHttpClient.BoundRequestBuilder put = getClient().preparePut(url);
    httpAuthentication.authenticate(put);

    put.setHeader("Content-Type", "application/vnd.scmm-cistatus+json;v=2")
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
    if (pullRequest && sourceBranch != null) {
      setReplacedBuild(buildStatus, jsonObject);
    }
    return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
  }

  private void setReplacedBuild(BuildStatus buildStatus, JSONObject jsonObject) {
    try {
      String[] path = buildStatus.getName().split("/");
      path[path.length - 1] = URLEncoder.encode(sourceBranch, "UTF-8");
      jsonObject.put("replaces", String.join("/", path));
    } catch (Exception e) {
      LOG.warn("Failed to compute replaced branch '{}' with path '{}'", sourceBranch, buildStatus.getName(), e);
    }
  }

  private String createUrl(String revision, BuildStatus buildStatus) throws UnsupportedEncodingException {
    return String.format(getUrl(),
      instance.toExternalForm(),
      namespaceAndName.getNamespace(),
      namespaceAndName.getName(),
      revision,
      buildStatus.getType(),
      URLEncoder.encode(buildStatus.getName(), StandardCharsets.UTF_8.name())
    );
  }

  private String getUrl() {
    return pullRequest? PULL_REQUEST_URL: CHANGESET_URL;
  }
}
