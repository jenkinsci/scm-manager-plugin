package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.HttpAuthentication;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

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
    HttpAuthentication noAuthentication = requestBuilder -> {
    };
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

  /**
   * This uses json mapping files from the resources (src/test/resources/mappings) and
   * answers requests with responses stored there.
   * These files had been created using wiremock on the first run.
   */
  private static class RecordedRequestDispatcher extends Dispatcher {
    private final File mappings = new File(getClass().getClassLoader().getResource("mappings").getFile());

    @Override
    public MockResponse dispatch(RecordedRequest request) {
      String requestedPath = request.getPath();
      return Arrays.stream(mappings.listFiles())
        .filter(
          file -> {
            try {
              JsonObject mapping = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
              JsonObject recoredRequest = mapping.get("request").getAsJsonObject();
              return requestedPath.endsWith(recoredRequest.get("url").getAsString())
                && recoredRequest.get("headers").getAsJsonObject().get("Accept").getAsJsonObject().get("equalTo").getAsString().equals(request.getHeader("Accept"));
            } catch (FileNotFoundException e) {
              throw new RuntimeException("failed to read mapping " + file, e);
            }
          }
        )
        .map(
          file -> {
            try {
              JsonObject mapping = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
              JsonObject recordedResponse = mapping.get("response").getAsJsonObject();
              MockResponse mockResponse = new MockResponse()
                .setResponseCode(recordedResponse.get("status").getAsInt())
                .setBody(recordedResponse.get("body").getAsString());
              JsonObject headers = recordedResponse.get("headers").getAsJsonObject();
              headers.keySet().forEach(
                h -> mockResponse.setHeader(h, headers.get(h).getAsString())
              );
              return mockResponse;
            } catch (FileNotFoundException e) {
              throw new RuntimeException("failed to read mapping " + file, e);
            }
          }
        )
        .findFirst()
        .orElseGet(() -> new MockResponse().setResponseCode(404));
    }
  }
}
