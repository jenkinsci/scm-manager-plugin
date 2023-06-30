package com.cloudogu.scmmanager;

import io.jenkins.plugins.okhttp.api.JenkinsOkHttpClient;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public final class OkHttpClientBuilder {

  private OkHttpClientBuilder() {
  }

  /**
   * Builds an {@link OkHttpClient} wrapped in {@link JenkinsOkHttpClient#newClientBuilder(OkHttpClient)}
   * for proxy support and with an increased timeout of one minute.
   */
  public static OkHttpClient build() {
    return JenkinsOkHttpClient.newClientBuilder(new OkHttpClient()).readTimeout(1L, TimeUnit.MINUTES).build();
  }
}
