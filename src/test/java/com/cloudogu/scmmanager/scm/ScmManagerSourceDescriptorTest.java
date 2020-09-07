package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.scm.api.Authentications;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.github.tomakehurst.wiremock.admin.NotFoundException;
import de.otto.edison.hal.HalRepresentation;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
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

import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ScmManagerSourceDescriptorTest {

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

  @Mock
  private Predicate<Repository> repositoryPredicate;

  @InjectMocks
  private TestingScmManagerSource.DescriptorImpl descriptor;

  @Before
  public void mockApiClient() {
    when(apiClientFactory.apply(requestedUrl.capture(), requestedAuthentication.capture())).thenReturn(api);
  }

  @Before
  public void mockAuthentications() {
    when(authenticationsProvider.apply(scmSourceOwner)).thenReturn(mockedAuthentication);
  }

  @Test
  public void shouldRejectEmptyServerUrl() throws InterruptedException, ExecutionException {
    FormValidation formValidation = descriptor.doCheckServerUrl("");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("server url is required");
  }

  @Test
  public void shouldRejectBlankServerUrl() throws InterruptedException, ExecutionException {
    FormValidation formValidation = descriptor.doCheckServerUrl("  \t");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("server url is required");
  }

  @Test
  public void shouldRejectNotWellFormedServerUrl() throws InterruptedException, ExecutionException {
    FormValidation formValidation = descriptor.doCheckServerUrl("http://");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("illegal URL format");
  }

  @Test
  public void shouldRejectServerUrlWithoutHttp() throws InterruptedException, ExecutionException {
    FormValidation formValidation = descriptor.doCheckServerUrl("file://some/where");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("Only http or https urls accepted");
  }

  @Test
  public void shouldRejectServerUrlWithoutLoginLink() throws InterruptedException, ExecutionException {
    HalRepresentation index = new HalRepresentation(linkingTo().single(link("any", "http://example.com/")).build());
    ScmManagerApiTestMocks.mockResult(when(api.index()), index);
    FormValidation formValidation = descriptor.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl.getValue()).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("api has no login link");
  }

//  @Test
//  public void shouldRejectServerUrlWithIllegalResponse() throws InterruptedException, ExecutionException {
//    mockError(new ApiClient.ApiError("could not parse response"), when(api.index()));
//
//    FormValidation formValidation = descriptor.doCheckServerUrl("http://example.com");
//
//    assertThat(requestedUrl.getValue()).isEqualTo("http://example.com");
//    assertThat(formValidation).isNotNull();
//    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
//    assertThat(formValidation.getMessage()).startsWith("could not parse response");
//  }
//
//  @Test
//  public void shouldRejectServerUrlThatCouldNotBeFound() throws InterruptedException, ExecutionException {
//    ScmManagerApiTestMocks.mockError(new ApiClient.ApiError(404), when(api.index()));
//
//    FormValidation formValidation = descriptor.doCheckServerUrl("http://example.com");
//
//    assertThat(requestedUrl.getValue()).isEqualTo("http://example.com");
//    assertThat(formValidation).isNotNull();
//    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
//    assertThat(formValidation.getMessage()).isEqualTo("illegal http status code: 404");
//  }

  @Test
  public void shouldAcceptServerUrl() throws InterruptedException, ExecutionException {
    mockCorrectIndex();
    FormValidation formValidation = descriptor.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl.getValue()).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.OK);
  }

  @Test
  public void shouldRejectEmptyCredentials() throws InterruptedException, ExecutionException {
    mockCorrectIndex();
    FormValidation formValidation = descriptor.validateCredentialsId(scmSourceOwner, "http://example.com", "", u -> mockedAuthentication);

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("credentials are required");
  }

  @Test
  public void shouldAcceptWorkingCredentials() throws InterruptedException, ExecutionException {
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
  public void shouldRejectWrongCredentials() throws InterruptedException, ExecutionException {
    mockCorrectIndex();
    HttpAuthentication authentication = x -> {};
    when(mockedAuthentication.from("http://example.com", "myAuth")).thenReturn(authentication);

    FormValidation formValidation = descriptor.validateCredentialsId(scmSourceOwner, "http://example.com", "myAuth", authenticationsProvider);

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("login failed");
  }

  @Test
  public void shouldNotLoadRepositoriesWhenServerUrlIsEmpty() throws InterruptedException, ExecutionException {
    ListBoxModel model = descriptor.doFillRepositoryItems(scmSourceOwner, "", "myAuth", null);

    assertThat(model.stream()).isEmpty();
  }

  @Test
  public void shouldNotLoadRepositoriesWhenCredentialsAreEmpty() throws InterruptedException, ExecutionException {
    ListBoxModel model = descriptor.doFillRepositoryItems(scmSourceOwner, "http://example.com", "", null);

    assertThat(model.stream()).isEmpty();
  }

  @Test
  public void shouldKeepSelectedRepositoryWhenAlreadySelected() throws InterruptedException, ExecutionException {
    ListBoxModel model = descriptor.doFillRepositoryItems(scmSourceOwner, "http://example.com", "", "hitchhiker/guide");

    assertThat(model.stream()).extracting("name").containsExactly("hitchhiker/guide");
  }

  @Test
  public void shouldReturnEmptyListOnError() throws InterruptedException, ExecutionException {
    ScmManagerApiTestMocks.mockError(new NotFoundException("not found"), when(api.getRepositories()));

    ListBoxModel model = descriptor.fillRepositoryItems(scmSourceOwner, "http://example.com", "myAuth", null, authenticationsProvider);

    assertThat(model.stream()).isEmpty();
  }

  @Test
  public void shouldReturnRepositories() throws InterruptedException, ExecutionException {
    when(repositoryPredicate.test(any())).thenReturn(true);
    ScmManagerApiTestMocks.mockResult(when(api.getRepositories()), asList(new Repository("space", "X", "git"), new Repository("blue", "dragon", "hg")));

    ListBoxModel model = descriptor.fillRepositoryItems(scmSourceOwner, "http://example.com", "myAuth", null, authenticationsProvider);

    assertThat(model.stream()).extracting("name").containsExactly("space/X (git)", "blue/dragon (hg)");
  }

  @Test
  public void shouldReturnFilteredRepositories() throws InterruptedException, ExecutionException {
    Repository spaceX = new Repository("space", "X", "git");
    Repository dragon = new Repository("blue", "dragon", "hg");

    when(repositoryPredicate.test(spaceX)).thenReturn(true);

    ScmManagerApiTestMocks.mockResult(when(api.getRepositories()), asList(spaceX, dragon));

    ListBoxModel model = descriptor.fillRepositoryItems(scmSourceOwner, "http://example.com", "myAuth", null, authenticationsProvider);

    assertThat(model.stream()).extracting("name").containsExactly("space/X (git)");
  }

  void mockCorrectIndex() {
    HalRepresentation index = new HalRepresentation(linkingTo().single(link("login", "http://example.com/")).build());
    ScmManagerApiTestMocks.mockResult(when(api.index()), index);
  }

  /**
   * We need an outer class to proper test a Descriptor.
   */
  public static class TestingScmManagerSource extends SCMSource {

    @Override
    protected void retrieve(SCMSourceCriteria criteria, SCMHeadObserver observer, SCMHeadEvent<?> event,  TaskListener listener) {

    }

    @Override
    public SCM build(SCMHead head, SCMRevision revision) {
      return null;
    }

    public static class DescriptorImpl extends ScmManagerSourceDescriptor {
      DescriptorImpl(BiFunction<String, HttpAuthentication, ScmManagerApi> apiFactory, Predicate<Repository> repositoryPredicate) {
        super(apiFactory, repositoryPredicate);
      }
    }
  }
}
