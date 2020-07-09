package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ApiClient;
import com.cloudogu.scmmanager.scm.api.ApiClientTestBase;
import hudson.util.FormValidation;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class ScmManagerSource_DescriptorImplTest extends ApiClientTestBase {

  private ScmManagerSource.DescriptorImpl descriptor = new ScmManagerSource.DescriptorImpl();

  private String requestedUrl;

  @Before
  public void mockApiClient() {
    Function<String, ApiClient> apiClientFactory = url -> {
      this.requestedUrl = url;
      return this.apiClient();
    };
    ScmManagerSource.DescriptorImpl.apiClientFactory = apiClientFactory;
  }

  @Test
  public void shouldRejectEmptyServerUrl() {
    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("server url is required");
  }

  @Test
  public void shouldRejectBlankServerUrl() {
    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("  \t");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("server url is required");
  }

  @Test
  public void shouldRejectServerUrlWithoutLoginLink() {
    injectPath("/noLogin");

    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("example.com");

    assertThat(requestedUrl).isEqualTo("example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("api has no login link");
  }

  @Test
  public void shouldRejectServerUrlWithIllegalResponse() {
    injectPath("/noJson");

    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("example.com");

    assertThat(requestedUrl).isEqualTo("example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("api has no login link");
  }

  @Test
  public void shouldAcceptServerUrl() {
    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("example.com");

    assertThat(requestedUrl).isEqualTo("example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.OK);
  }
}
