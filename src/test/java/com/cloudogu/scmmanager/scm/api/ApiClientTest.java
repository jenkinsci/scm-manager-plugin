package com.cloudogu.scmmanager.scm.api;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiClientTest extends ApiClientTestBase {

  @Test
  public void shouldReturnMockedData() throws InterruptedException {
    ApiClient api = apiClient();

    SomeDataClass data = api.get("/some/thing", "application/json", SomeDataClass.class)
      .mapError(e -> null);

    assertThat(data).isNotNull();
    assertThat(data.someString).isEqualTo("stringValue");
    assertThat(data.someNumber).isEqualTo(42);
  }

  @Test
  public void shouldTrimServerUrl() {
    String fixedUrl = ApiClient.fixServerUrl("  http://hitchhiker.com/scm\t").apply("/api");

    assertThat(fixedUrl).isEqualTo("http://hitchhiker.com/scm/api");
  }

  @Test
  public void shouldRemoveTrailingSlashFromServerUrl() {
    String fixedUrl = ApiClient.fixServerUrl("http://hitchhiker.com/scm/").apply("/api");

    assertThat(fixedUrl).isEqualTo("http://hitchhiker.com/scm/api");
  }

  public static class SomeDataClass {
    public String someString;
    public int someNumber;
  }
}
