package com.cloudogu.scmmanager.scm.api;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SshApiClientTest {

  @Test
  public void shouldCompleteReturnApiUrl() {
    String apiUrl = SshApiClient.createApiUrl("https://hitchhiker.com/scm/api/v2", "/api/v2/repositories");
    assertThat(apiUrl).isEqualTo("https://hitchhiker.com/scm/api/v2/repositories");
  }

  @Test
  public void shouldRemoveEndingSlashFromApiUrl() {
    String apiUrl = SshApiClient.createApiUrl("https://hitchhiker.com/scm/api/v2/", "/api/v2/repositories");
    assertThat(apiUrl).isEqualTo("https://hitchhiker.com/scm/api/v2/repositories");
  }

  @Test
  public void shouldNotModifyCompleteUrls() {
    String apiUrl = SshApiClient.createApiUrl("https://hitchhiker.com/scm/api/v2/", "https://scm.hitchhiker.com/scm/api/v2/repositories");
    assertThat(apiUrl).isEqualTo("https://scm.hitchhiker.com/scm/api/v2/repositories");
  }

}
