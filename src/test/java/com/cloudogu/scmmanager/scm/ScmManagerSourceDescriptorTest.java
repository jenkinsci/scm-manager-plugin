package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.IllegalReturnStatusException;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import hudson.model.Item;
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
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Link.linkBuilder;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ScmManagerSourceDescriptorTest {

  @Mock
  private SCMSourceOwner scmSourceOwner;

  @Captor
  private ArgumentCaptor<String> requestedUrl;

  @Captor
  private ArgumentCaptor<String> requestedCredentials;

  @Mock
  private ScmManagerApiFactory apiFactory;

  @Mock
  private ScmManagerApi api;

  @Mock
  private Predicate<Repository> repositoryPredicate;

  @InjectMocks
  private TestingScmManagerSource.DescriptorImpl descriptor;

  @Before
  public void mockApiClient() {
    when(apiFactory.create(any(Item.class), requestedUrl.capture(), requestedCredentials.capture())).thenReturn(api);
    when(apiFactory.anonymous(requestedUrl.capture())).thenReturn(api);

    when(api.getProtocol()).thenReturn("http");
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
    assertThat(formValidation.getMessage()).isEqualTo("Only http, https or ssh urls accepted");
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

  @Test
  public void shouldHandleRedirectResponseForIndexRequest() throws InterruptedException, ExecutionException {
        ScmManagerApiTestMocks.mockError(new CompletionException(new IllegalReturnStatusException(302)), when(api.index()));

    FormValidation formValidation = descriptor.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl.getValue()).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.OK);
    assertThat(formValidation.getMessage()).isEqualTo("Credentials needed");
  }

  @Test
  public void shouldRejectServerUrlThatCouldNotBeFound() throws InterruptedException, ExecutionException {
    ScmManagerApiTestMocks.mockError(new RuntimeException("not found"), when(api.index()));

    FormValidation formValidation = descriptor.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl.getValue()).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("not found");
  }

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
    FormValidation formValidation = descriptor.validateCredentialsId(scmSourceOwner, "http://example.com", "");

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
    FormValidation formValidation = descriptor.validateCredentialsId(scmSourceOwner, "http://example.com", "myAuth");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.OK);
    assertThat(requestedCredentials.getValue()).isEqualTo("myAuth");
  }

  @Test
  public void shouldRejectWrongCredentials() throws InterruptedException, ExecutionException {
    mockCorrectIndex();

    FormValidation formValidation = descriptor.validateCredentialsId(scmSourceOwner, "http://example.com", "myAuth");

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
    ScmManagerApiTestMocks.mockError(new RuntimeException("not found"), when(api.getRepositories()));

    ListBoxModel model = descriptor.fillRepositoryItems(scmSourceOwner, "http://example.com", "myAuth", null);

    assertThat(model.stream()).isEmpty();
  }

  @Test
  public void shouldReturnRepositories() throws InterruptedException, ExecutionException {
    when(repositoryPredicate.test(any())).thenReturn(true);
    ScmManagerApiTestMocks.mockResult(when(api.getRepositories()), asList(createSpaceX(), createDragon()));

    ListBoxModel model = descriptor.fillRepositoryItems(scmSourceOwner, "http://example.com", "myAuth", null);

    assertThat(model.stream()).extracting("name").containsExactly("space/X (git)", "blue/dragon (hg)");
  }

  @Test
  public void shouldReturnFilteredRepositories() throws InterruptedException, ExecutionException {
    Repository spaceX = createSpaceX();
    Repository dragon = createDragon();
    Repository hog = createHoG();

    when(repositoryPredicate.test(spaceX)).thenAnswer(ic -> {
      Repository repository = ic.getArgument(0);
      return "git".equals(repository.getType());
    });

    ScmManagerApiTestMocks.mockResult(when(api.getRepositories()), asList(spaceX, dragon, hog));

    ListBoxModel model = descriptor.fillRepositoryItems(scmSourceOwner, "http://example.com", "myAuth", null);

    assertThat(model.stream()).extracting("name").containsExactly("space/X (git)");
  }

  private Repository createHoG() {
    return new Repository("hitchhiker", "hog", "git", sshLinks());
  }

  private Repository createDragon() {
    return new Repository("blue", "dragon", "hg", httpLinks());
  }

  private Repository createSpaceX() {
    return new Repository("space", "X", "git", bothLinks());
  }

  private Links bothLinks() {
    return linkingTo().array(httpLink(), sshLink()).build();
  }

  private Links httpLinks() {
    return linkingTo().single(httpLink()).build();
  }

  private Links sshLinks() {
    return linkingTo().single(sshLink()).build();
  }

  private Link httpLink() {
    return linkBuilder("protocol", "https://hitchhiker.com/scm").withName("http").build();
  }

  private Link sshLink() {
    return linkBuilder("protocol", "ssh://hitchhiker.com/scm").withName("ssh").build();
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
      DescriptorImpl(ScmManagerApiFactory apiFactory, Predicate<Repository> repositoryPredicate) {
        super(apiFactory, repositoryPredicate);
      }
    }
  }
}
