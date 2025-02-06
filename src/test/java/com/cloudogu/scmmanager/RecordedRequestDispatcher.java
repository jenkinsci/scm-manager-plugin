package com.cloudogu.scmmanager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * This uses json mapping files from the resources (src/test/resources/mappings) and
 * answers requests with responses stored there.
 * These files had been created using wiremock on the first run.
 */
public class RecordedRequestDispatcher extends Dispatcher {
    private final File mappings =
            new File(getClass().getClassLoader().getResource("mappings").getFile());

    @Override
    public MockResponse dispatch(RecordedRequest request) {
        String requestedPath = request.getPath();
        return Arrays.stream(mappings.listFiles())
                .filter(file -> {
                    try {
                        JsonObject mapping =
                                JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
                        JsonObject recordedRequest = mapping.get("request").getAsJsonObject();
                        JsonElement acceptNode =
                                recordedRequest.get("headers").getAsJsonObject().get("Accept");
                        return requestedPath.endsWith(recordedRequest.get("url").getAsString())
                                && (acceptNode == null
                                        || acceptNode
                                                .getAsJsonObject()
                                                .get("equalTo")
                                                .getAsString()
                                                .equals(request.getHeader("Accept")));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("failed to read mapping " + file, e);
                    }
                })
                .map(file -> {
                    try {
                        JsonObject mapping =
                                JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
                        JsonObject recordedResponse = mapping.get("response").getAsJsonObject();
                        MockResponse mockResponse = new MockResponse()
                                .setResponseCode(recordedResponse.get("status").getAsInt())
                                .setBody(recordedResponse.get("body").getAsString());
                        JsonObject headers = recordedResponse.get("headers").getAsJsonObject();
                        headers.keySet()
                                .forEach(h ->
                                        mockResponse.setHeader(h, headers.get(h).getAsString()));
                        return mockResponse;
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("failed to read mapping " + file, e);
                    }
                })
                .findFirst()
                .orElseGet(() -> {
                    System.out.println("no response found for request path " + request.getPath());
                    return new MockResponse().setResponseCode(404);
                });
    }
}
