package com.cloudogu.scmmanager.scm.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;
import org.junit.Test;

public class HttpApiClientTest extends ApiClientTestBase {

    @Test
    public void shouldReturnMockedData() throws InterruptedException, ExecutionException {
        ApiClient api = apiClient();

        SomeDataClass data =
                api.get("/some/thing", "application/json", SomeDataClass.class).get();

        assertThat(data).isNotNull();
        assertThat(data.someString).isEqualTo("stringValue");
        assertThat(data.someNumber).isEqualTo(42);
    }

    @Test
    public void shouldMapData() throws InterruptedException, ExecutionException {
        ApiClient api = apiClient();

        String message = api.get("/some/thing", "application/json", SomeDataClass.class)
                .thenApply(data -> data.someString)
                .get();

        assertThat(message).isNotNull();
        assertThat(message).isEqualTo("stringValue");
    }

    //  @Test
    //  public void shouldFetchMismatchedInputException() throws InterruptedException, ExecutionException {
    //    ApiClient api = apiClient();
    //
    //    SomeOtherClass data = api.get("/some/thing", "application/json", SomeOtherClass.class)
    //      .exceptionally(e -> new SomeOtherClass(e.getMessage())).get();
    //
    //    assertThat(data.different).contains("could not parse response", "MismatchedInputException");
    //  }
    //
    //  @Test
    //  public void shouldFetchJsonParseException() throws InterruptedException {
    //    ApiClient api = apiClient();
    //
    //    String data = api.get("/no/json", "application/json", String.class)
    //      .mapError(e -> e.getMessage());
    //
    //    assertThat(data).contains("could not parse response", "JsonParseException");
    //  }
    //
    //  @Test
    //  public void shouldRejectServerUrlThatCouldNotBeFound() throws InterruptedException {
    //    injectPath("/notFound");
    //
    //    SomeDataClass someData = apiClient()
    //      .get("/api/v2", "application/json", SomeDataClass.class)
    //      .mapError(e -> new SomeDataClass(e.getMessage(), e.getStatus()));
    //
    //    assertThat(someData.someNumber).isEqualTo(404);
    //    assertThat(someData.someString).isEqualTo("illegal http status code: 404");
    //  }

    @Test
    public void shouldTrimServerUrl() {
        String fixedUrl =
                HttpApiClient.fixServerUrl("  http://hitchhiker.com/scm\t").apply("/api");

        assertThat(fixedUrl).isEqualTo("http://hitchhiker.com/scm/api");
    }

    @Test
    public void shouldRemoveTrailingSlashFromServerUrl() {
        String fixedUrl =
                HttpApiClient.fixServerUrl("http://hitchhiker.com/scm/").apply("/api");

        assertThat(fixedUrl).isEqualTo("http://hitchhiker.com/scm/api");
    }

    @Test
    public void shouldNotChangeAbsoluteUrl() {
        String fixedUrl =
                HttpApiClient.fixServerUrl("http://hitchhiker.com/scm/").apply("http://vogon.vo/destroy");

        assertThat(fixedUrl).isEqualTo("http://vogon.vo/destroy");
    }

    public static class SomeDataClass {
        public String someString;
        public int someNumber;

        public SomeDataClass() {}

        public SomeDataClass(String someString, int someNumber) {
            this.someString = someString;
            this.someNumber = someNumber;
        }
    }

    public static class SomeOtherClass {
        public String different;

        public SomeOtherClass(String different) {
            this.different = different;
        }
    }
}
