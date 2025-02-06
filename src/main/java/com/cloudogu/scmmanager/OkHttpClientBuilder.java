package com.cloudogu.scmmanager;

import io.jenkins.plugins.okhttp.api.JenkinsOkHttpClient;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OkHttpClientBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(OkHttpClientBuilder.class);

    private OkHttpClientBuilder() {}

    /**
     * Builds an {@link OkHttpClient} wrapped in {@link JenkinsOkHttpClient#newClientBuilder(OkHttpClient)}
     * for proxy support and with an increased timeout of one minute.
     */
    public static OkHttpClient build() {
        return JenkinsOkHttpClient.newClientBuilder(new OkHttpClient())
                .readTimeout(1L, TimeUnit.MINUTES)
                .eventListener(new EventListener() {
                    @Override
                    public void callFailed(Call call, IOException ioe) {
                        LOG.warn("SCM-Manager request failed", ioe);
                    }
                })
                .followRedirects(false)
                .build();
    }
}
