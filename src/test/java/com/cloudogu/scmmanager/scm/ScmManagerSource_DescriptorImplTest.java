package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.scm.api.ApiClient;
import com.cloudogu.scmmanager.scm.api.ApiClientTestBase;
import com.cloudogu.scmmanager.scm.api.Authentications;
import hudson.util.FormValidation;
import jenkins.scm.api.SCMSourceOwner;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScmManagerSource_DescriptorImplTest extends ApiClientTestBase {

  private ScmManagerSource.DescriptorImpl descriptor = new ScmManagerSource.DescriptorImpl();

  private final SCMSourceOwner scmSourceOwner = Mockito.mock(SCMSourceOwner.class);
  private final Authentications mockedAuthentication = Mockito.mock(Authentications.class);

  private String requestedUrl;
  private HttpAuthentication requestedAuthentication;

  @Before
  public void mockApiClient() {
    BiFunction<String, HttpAuthentication, ApiClient> apiClientFactory = (url, auth) -> {
      this.requestedUrl = url;
      this.requestedAuthentication = auth;
      return this.apiClient();
    };
    ScmManagerSource.DescriptorImpl.apiClientFactory = apiClientFactory;
  }

  @Test
  public void shouldRejectEmptyServerUrl() throws InterruptedException {
    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("server url is required");
  }

  @Test
  public void shouldRejectBlankServerUrl() throws InterruptedException {
    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("  \t");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("server url is required");
  }

  @Test
  public void shouldRejectNotWellFormedServerUrl() throws InterruptedException {
    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("http://");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("illegal URL format");
  }

  @Test
  public void shouldRejectServerUrlWithoutHttp() throws InterruptedException {
    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("file://some/where");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("Only http or https urls accepted");
  }

  @Test
  public void shouldRejectServerUrlWithoutLoginLink() throws InterruptedException {
    injectPath("/noLogin");

    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("api has no login link");
  }

  @Test
  public void shouldRejectServerUrlWithIllegalResponse() throws InterruptedException {
    injectPath("/noJson");

    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).startsWith("could not parse response");
  }

  @Test
  public void shouldRejectServerUrlThatCouldNotBeFound() throws InterruptedException {
    injectPath("/notFound");

    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("illegal http status code: 404");
  }

  @Test
  public void shouldAcceptServerUrl() throws InterruptedException {
    FormValidation formValidation = ScmManagerSource.DescriptorImpl.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.OK);
  }

  @Test
  public void shouldRejectEmptyCredentials() throws InterruptedException {
    FormValidation formValidation = ScmManagerSource.DescriptorImpl.validateCredentialsId(scmSourceOwner, "http://example.com", "", u -> mockedAuthentication);

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("credentials are required");
  }

  @Test
  public void shouldAcceptWorkingCredentials() throws InterruptedException {
    injectPath("", "/loggedIn");
    SCMSourceOwner scmSourceOwner = Mockito.mock(SCMSourceOwner.class);
    Authentications mockedAuthentication = Mockito.mock(Authentications.class);
    HttpAuthentication authentication = x -> {};
    when(mockedAuthentication.from("http://example.com", "myAuth")).thenReturn(authentication);
    Function<SCMSourceOwner, Authentications> authenticationsProvider = mock(Function.class);
    when(authenticationsProvider.apply(scmSourceOwner)).thenReturn(mockedAuthentication);

    FormValidation formValidation = ScmManagerSource.DescriptorImpl.validateCredentialsId(scmSourceOwner, "http://example.com", "myAuth", authenticationsProvider);

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.OK);
    assertThat(requestedAuthentication).isSameAs(authentication);
  }

  @Test
  public void shouldRejectWrongCredentials() throws InterruptedException {
//    injectPath("", "/loginFailed");
    SCMSourceOwner scmSourceOwner = Mockito.mock(SCMSourceOwner.class);
    Authentications mockedAuthentication = Mockito.mock(Authentications.class);
    HttpAuthentication authentication = x -> {};
    when(mockedAuthentication.from("http://example.com", "myAuth")).thenReturn(authentication);
    Function<SCMSourceOwner, Authentications> authenticationsProvider = mock(Function.class);
    when(authenticationsProvider.apply(scmSourceOwner)).thenReturn(mockedAuthentication);

    FormValidation formValidation = ScmManagerSource.DescriptorImpl.validateCredentialsId(scmSourceOwner, "http://example.com", "myAuth", authenticationsProvider);

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("login failed");
  }
}
