package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.scm.api.ApiClient;
import com.cloudogu.scmmanager.scm.api.Authentications;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import de.otto.edison.hal.HalRepresentation;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMSourceOwner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.cloudogu.scmmanager.scm.ScmManagerApiTestMocks.mockError;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmManagerSource_DescriptorImplTest  {

  @Mock
  private SCMSourceOwner scmSourceOwner;
  @Mock
  private Authentications mockedAuthentication;
  @Mock
  private Function<SCMSourceOwner, Authentications> authenticationsProvider;

  @Captor
  private ArgumentCaptor<String> requestedUrl;
  @Captor
  private ArgumentCaptor<HttpAuthentication> requestedAuthentication;

  @Spy
  private BiFunction<String, HttpAuthentication, ScmManagerApi> apiClientFactory;
  @Mock
  private ScmManagerApi api;
  @InjectMocks
  private ScmManagerSource.DescriptorImpl descriptor;

  @Before
  public void mockApiClient() {
    when(apiClientFactory.apply(requestedUrl.capture(), requestedAuthentication.capture())).thenReturn(api);
  }

  @Before
  public void mockAuthentications() {
    when(authenticationsProvider.apply(scmSourceOwner)).thenReturn(mockedAuthentication);
  }

  @Test
  public void shouldRejectEmptyServerUrl() throws InterruptedException {
    FormValidation formValidation = descriptor.doCheckServerUrl("");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("server url is required");
  }

  @Test
  public void shouldRejectBlankServerUrl() throws InterruptedException {
    FormValidation formValidation = descriptor.doCheckServerUrl("  \t");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("server url is required");
  }

  @Test
  public void shouldRejectNotWellFormedServerUrl() throws InterruptedException {
    FormValidation formValidation = descriptor.doCheckServerUrl("http://");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("illegal URL format");
  }

  @Test
  public void shouldRejectServerUrlWithoutHttp() throws InterruptedException {
    FormValidation formValidation = descriptor.doCheckServerUrl("file://some/where");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("Only http or https urls accepted");
  }

  @Test
  public void shouldRejectServerUrlWithoutLoginLink() throws InterruptedException {
    HalRepresentation index = new HalRepresentation(linkingTo().single(link("any", "http://example.com/")).build());
    ScmManagerApiTestMocks.mockResult(when(api.index()), index);
    FormValidation formValidation = descriptor.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl.getValue()).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("api has no login link");
  }

  @Test
  public void shouldRejectServerUrlWithIllegalResponse() throws InterruptedException {
    mockError(new ApiClient.ApiError("could not parse response"), when(api.index()));

    FormValidation formValidation = descriptor.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl.getValue()).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).startsWith("could not parse response");
  }

  @Test
  public void shouldRejectServerUrlThatCouldNotBeFound() throws InterruptedException {
    ScmManagerApiTestMocks.mockError(new ApiClient.ApiError(404), when(api.index()));

    FormValidation formValidation = descriptor.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl.getValue()).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("illegal http status code: 404");
  }

  @Test
  public void shouldAcceptServerUrl() throws InterruptedException {
    mockCorrectIndex();
    FormValidation formValidation = descriptor.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl.getValue()).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.OK);
  }

  @Test
  public void shouldRejectEmptyCredentials() throws InterruptedException {
    mockCorrectIndex();
    FormValidation formValidation = descriptor.validateCredentialsId(scmSourceOwner, "http://example.com", "", u -> mockedAuthentication);

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("credentials are required");
  }

  @Test
  public void shouldAcceptWorkingCredentials() throws InterruptedException {
    HalRepresentation index = new HalRepresentation(linkingTo().single(link("login", "http://example.com/")).build());
    HalRepresentation indexWithLogIn = new HalRepresentation(linkingTo().single(link("me", "http://example.com/")).build());
    ScmManagerApiTestMocks.mockResult(when(api.index()), index, indexWithLogIn);

    SCMSourceOwner scmSourceOwner = Mockito.mock(SCMSourceOwner.class);
    Authentications mockedAuthentication = Mockito.mock(Authentications.class);
    HttpAuthentication authentication = x -> {};
    when(mockedAuthentication.from("http://example.com", "myAuth")).thenReturn(authentication);
    Function<SCMSourceOwner, Authentications> authenticationsProvider = mock(Function.class);
    when(authenticationsProvider.apply(scmSourceOwner)).thenReturn(mockedAuthentication);

    FormValidation formValidation = descriptor.validateCredentialsId(scmSourceOwner, "http://example.com", "myAuth", authenticationsProvider);

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.OK);
    assertThat(requestedAuthentication.getValue()).isSameAs(authentication);
  }

  @Test
  public void shouldRejectWrongCredentials() throws InterruptedException {
    mockCorrectIndex();
    HttpAuthentication authentication = x -> {};
    when(mockedAuthentication.from("http://example.com", "myAuth")).thenReturn(authentication);

    FormValidation formValidation = descriptor.validateCredentialsId(scmSourceOwner, "http://example.com", "myAuth", authenticationsProvider);

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("login failed");
  }

  @Test
  public void shouldNotLoadRepositoriesWhenServerUrlIsEmpty() throws InterruptedException {
    ListBoxModel model = descriptor.doFillRepositoryItems(scmSourceOwner, "", "myAuth", null);

    assertThat(model.stream()).isEmpty();
  }

  @Test
  public void shouldNotLoadRepositoriesWhenCredentialsAreEmpty() throws InterruptedException {
    ListBoxModel model = descriptor.doFillRepositoryItems(scmSourceOwner, "http://example.com", "", null);

    assertThat(model.stream()).isEmpty();
  }

  @Test
  public void shouldKeepSelectedRepositoryWhenAlreadySelected() throws InterruptedException {
    ListBoxModel model = descriptor.doFillRepositoryItems(scmSourceOwner, "http://example.com", "", "hitchhiker/guide");

    assertThat(model.stream()).extracting("name").containsExactly("hitchhiker/guide");
  }

  @Test
  public void shouldReturnEmptyListOnError() throws InterruptedException {
    ScmManagerApiTestMocks.mockError(new ApiClient.ApiError(404), when(api.getRepositories()));

    ListBoxModel model = descriptor.fillRepositoryItems(scmSourceOwner, "http://example.com", "myAuth", null, authenticationsProvider);

    assertThat(model.stream()).isEmpty();
  }

  @Test
  public void shouldReturnRepositories() throws InterruptedException {
    ScmManagerApiTestMocks.mockResult(when(api.getRepositories()), asList(new Repository("space", "X", "git"), new Repository("blue", "dragon", "hg")));

    ListBoxModel model = descriptor.fillRepositoryItems(scmSourceOwner, "http://example.com", "myAuth", null, authenticationsProvider);

    assertThat(model.stream()).extracting("name").containsExactly("space/X (git)", "blue/dragon (hg)");
  }

  void mockCorrectIndex() {
    HalRepresentation index = new HalRepresentation(linkingTo().single(link("login", "http://example.com/")).build());
    ScmManagerApiTestMocks.mockResult(when(api.index()), index);
  }
}
