package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.RecordedRequestDispatcher;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;

public class ApiClientTestBase {

    private final MockWebServer server = new MockWebServer();

    private OkHttpClient client;

    private String[] pathInjection = {};
    private int pathInjectionIndex = 0;

    @Before
    public void setUpServerAndClient() throws IOException {
        client = new OkHttpClient();

        Dispatcher mDispatcher = new RecordedRequestDispatcher();
        server.setDispatcher(mDispatcher);
        server.start();
    }

    public OkHttpClient getClient() {
        return client;
    }

    protected ApiClient apiClient() {
        HttpAuthentication noAuthentication = requestBuilder -> {};
        return new HttpApiClient(client, noAuthentication, this::serverUrl);
    }

    protected String serverUrl(String path) {
        return String.format("http://localhost:%d%s", server.getPort(), nextPathInjection() + path);
    }

    private String nextPathInjection() {
        if (pathInjection.length == 0) {
            return "";
        }
        if (pathInjection.length == 1) {
            return pathInjection[0];
        }
        return pathInjection[pathInjectionIndex++];
    }
}
