package com.cloudogu.scmmanager.scm.api;

import com.cloudogu.scmmanager.HttpAuthentication;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiClientTest extends ApiClientTestBase {

  @Test
  public void shouldReturnMockedData() throws ExecutionException, InterruptedException {
    ApiClient api = apiClient();

    SomeDataClass data = api.get("/some/thing", "application/json", SomeDataClass.class).get();

    assertThat(data).isNotNull();
    assertThat(data.someString).isEqualTo("stringValue");
    assertThat(data.someNumber).isEqualTo(42);
  }

  public static class SomeDataClass {
    public String someString;
    public int someNumber;
  }
}
