package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.JobInformation;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import hudson.model.Run;
import io.jenkins.plugins.okhttp.api.JenkinsOkHttpClient;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class ScmMigratedV1Notifier implements Notifier {

    private static final Logger LOG = LoggerFactory.getLogger(ScmMigratedV1Notifier.class);
    private final AuthenticationFactory authenticationFactory;
    private final Run<?, ?> run;
    private final JobInformation information;

    private OkHttpClient client;
    private ScmV2NotifierProvider v2NotifierProvider;

    ScmMigratedV1Notifier(AuthenticationFactory authenticationFactory, Run<?, ?> run, JobInformation information) {
        this.authenticationFactory = authenticationFactory;
        this.run = run;
        this.information = information;
    }

    @VisibleForTesting
    void setClient(OkHttpClient client) {
        this.client = client;
    }

    private OkHttpClient getClient() {
        if (client != null) {
            return client;
        }
        return JenkinsOkHttpClient.newClientBuilder(new OkHttpClient.Builder().followRedirects(false).build()).build();
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
        Request.Builder request = new Request.Builder().url(information.getUrl()).get();
        authenticationFactory.createHttp(run, information.getCredentialsId()).authenticate(request);
        getClient().newCall(request.build()).enqueue(new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (response) {
                    if (response.isRedirect()) {
                        String location = response.header("Location");
                        if (!Strings.isNullOrEmpty(location)) {
                            notifyV2(location, revision, buildStatus);
                        } else {
                            LOG.warn("server returned redirect without location header");
                        }
                    } else {
                        LOG.debug("expected redirect, but server returned status code {}", response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                LOG.warn("failed to get redirect uri from migrated repository", e);
            }
        });
    }

    private void notifyV2(String location, String revision, BuildStatus buildStatus) throws IOException {
        ScmV2NotifierProvider provider = getV2NotifierProvider();
        provider.setAuthenticationFactory(authenticationFactory);

        JobInformation redirectedInformation = new JobInformation(
            information.getType(),
            location,
            revision,
            information.getCredentialsId(),
            false);

        Optional<ScmV2Notifier> scmV2Notifier = provider.get(run, redirectedInformation);
        if (scmV2Notifier.isPresent()) {
            LOG.debug("notify v2 url {}", location);
            scmV2Notifier.get().notify(revision, buildStatus);
        } else {
            LOG.debug("redirect uri {} does not look like a scm v2 repo url", location);
        }
    }
}
