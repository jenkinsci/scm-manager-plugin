package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.IllegalReturnStatusException;
import com.cloudogu.scmmanager.scm.api.Index;
import com.cloudogu.scmmanager.scm.api.Namespace;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import hudson.model.AutoCompletionCandidates;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.util.FormValidation;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceOwner;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Link.linkBuilder;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
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

  static final String MODERN_VERSION = "3.7.2";
  static final String LEGACY_VERSION = "2.46.2";
  static final String SNAPSHOT_VERSION = "4.196.11-CUSTOMENDING.HERE";

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
    mockEmptyIndex();

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
    mockCorrectIndexForVersion(MODERN_VERSION);
    FormValidation formValidation = descriptor.doCheckServerUrl("http://example.com");

    assertThat(requestedUrl.getValue()).isEqualTo("http://example.com");
    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.OK);
  }

  @Test
  public void shouldRejectEmptyCredentials() throws InterruptedException, ExecutionException {
    mockCorrectIndexForVersion(MODERN_VERSION);
    FormValidation formValidation = descriptor.validateCredentialsId(scmSourceOwner, "http://example.com", "");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("credentials are required");
  }

  @Test
  public void shouldAcceptWorkingCredentials() throws InterruptedException, ExecutionException {
    Index index = new Index(linkingTo().single(link("login", "http://example.com/")).build());
    Index indexWithLogIn = new Index(linkingTo().single(link("me", "http://example.com/")).build());
    ScmManagerApiTestMocks.mockResult(when(api.index()), index, indexWithLogIn);

    SCMSourceOwner scmSourceOwner = Mockito.mock(SCMSourceOwner.class);
    FormValidation formValidation = descriptor.validateCredentialsId(scmSourceOwner, "http://example.com", "myAuth");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.OK);
    assertThat(requestedCredentials.getValue()).isEqualTo("myAuth");
  }

  @Test
  public void shouldRejectWrongCredentials() throws InterruptedException, ExecutionException {
    mockCorrectIndexForVersion(MODERN_VERSION);

    FormValidation formValidation = descriptor.validateCredentialsId(scmSourceOwner, "http://example.com", "myAuth");

    assertThat(formValidation).isNotNull();
    assertThat(formValidation.kind).isEqualTo(FormValidation.Kind.ERROR);
    assertThat(formValidation.getMessage()).isEqualTo("login failed");
  }

  @Test
  public void shouldNotLoadRepositoriesWhenServerUrlIsEmpty() throws InterruptedException, ExecutionException {
    AutoCompletionCandidates candidates = descriptor.autoCompleteRepository(scmSourceOwner, "", "myAuth", null);

    assertThat(candidates.getValues()).isEmpty();
  }

  @Test
  public void shouldNotLoadRepositoriesWhenCredentialsAreEmpty() throws InterruptedException, ExecutionException {
    AutoCompletionCandidates candidates = descriptor.autoCompleteRepository(scmSourceOwner, "http://example.com", "", null);

    assertThat(candidates.getValues()).isEmpty();
  }

  @Test
  public void shouldReturnEmptyListOnError() throws InterruptedException, ExecutionException {
    mockCorrectIndexForVersion(MODERN_VERSION);

    ScmManagerApiTestMocks.mockError(new RuntimeException("not found"), when(api.getRepositories(any(ScmManagerApi.SearchQuery.class))));

    AutoCompletionCandidates candidates = descriptor.autoCompleteRepository(scmSourceOwner, "http://example.com", "myAuth", null);

    assertThat(candidates.getValues()).isEmpty();
  }

  @Test
  public void shouldReturnRepositories() throws InterruptedException, ExecutionException {
    mockCorrectIndexForVersion(MODERN_VERSION);

    Repository spaceX = createSpaceX();
    Repository dragon = createDragon();

    addRepositories(spaceX, dragon);

    AutoCompletionCandidates candidates = descriptor.autoCompleteRepository(scmSourceOwner, "http://example.com", "myAuth", null);

    assertThat(candidates.getValues()).containsExactly("space/X", "blue/dragon");
  }

  @Test
  public void shouldReturnRepositoriesWithNamespaceAndSlash() throws InterruptedException, ExecutionException {
    // TODO FIX
    mockCorrectIndexForVersion(MODERN_VERSION);

    Repository spaceX = createSpaceX();

    addRepositories(spaceX);

    when(repositoryPredicate.test(spaceX)).thenAnswer(ic -> {
      Repository repository = ic.getArgument(0);
      return "git".equals(repository.getType());
    });

    AutoCompletionCandidates candidates = descriptor.autoCompleteRepository(scmSourceOwner, "http://example.com", "myAuth", "blue/");

    assertThat(candidates.getValues()).containsExactly("space/X");
  }


  @Test
  public void shouldReturnFilteredRepositories() throws InterruptedException, ExecutionException {
    mockCorrectIndexForVersion(MODERN_VERSION);

    Repository spaceX = createSpaceX();
    Repository dragon = createDragon();
    Repository hog = createHoG();

    addRepositories(spaceX, dragon, hog);

    when(repositoryPredicate.test(spaceX)).thenAnswer(ic -> {
      Repository repository = ic.getArgument(0);
      return "git".equals(repository.getType());
    });

    AutoCompletionCandidates candidates = descriptor.autoCompleteRepository(scmSourceOwner, "http://example.com", "myAuth", null);

    assertThat(candidates.getValues()).containsExactly("space/X");
  }


  @Test
  public void shouldNotReturnRepositoryWithIncompleteNamespaceInLegacyVersion() throws InterruptedException, ExecutionException {
    mockCorrectIndexForVersion(LEGACY_VERSION);
    Repository dragon = createDragon();

    addRepositories(dragon);

    AutoCompletionCandidates candidates = descriptor.autoCompleteRepository(scmSourceOwner, "http://example.com", "myAuth", "blu");

    assertThat(candidates.getValues()).isEmpty();
  }

  @Test
  public void shouldNotReturnMoreThanFiveRepositoriesInModernRepositories() throws ExecutionException, InterruptedException {
    // TODO FIX
    mockCorrectIndexForVersion(MODERN_VERSION);

    Repository hog = createHoG();
    Repository scotty = createScotty();
    Repository spock = createSpock();
    Repository worf = createWorf();
    Repository dragon = createDragon(); // hg > not supposed to be in with filter
    Repository spaceX = createSpaceX();
    Repository vader = createVader(); // exceding 5

    when(repositoryPredicate.test(any(Repository.class))).thenAnswer(ic -> {
      Repository repository = ic.getArgument(0);
      return "git".equals(repository.getType());
    });

    addRepositories(hog, scotty, spock, worf, dragon, spaceX, vader);

    AutoCompletionCandidates candidates = descriptor.autoCompleteRepository(scmSourceOwner, "http://example.com", "myAuth", "");

    assertThat(candidates.getValues()).containsExactly(
      "hitchhiker/hog", "enterprise/scotty", "enterprise/spock", "clingon/worf", "space/X");
  }


  @Test
  public void shouldThrowWarningAfterTimeout() throws Exception {
    mockCorrectIndexForVersion(MODERN_VERSION);

    Repository spaceX = createSpaceX();
    Repository dragon = createDragon();

    addRepositories(spaceX, dragon);

    CompletableFuture<List<Repository>> verySlowFuture = spy(new CompletableFuture<>());
    doThrow(TimeoutException.class).when(verySlowFuture).get(anyInt(), any(TimeUnit.class));
    when(api.getRepositories(any(ScmManagerApi.SearchQuery.class))).thenReturn(verySlowFuture);


    assertThatThrownBy(() -> descriptor.autoCompleteRepository(scmSourceOwner, "http://veryslowexample.com", "myAuth", "blue"),
      "Test timeout code").isInstanceOf(ExecutionException.class).hasMessage("Repository loading failed due to a timeout or load issue by the SCM-Manager instance.");
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

  private Repository createSpock() {
    return new Repository("enterprise", "spock", "git", httpLinks());
  }

  private Repository createScotty() {
    return new Repository("enterprise", "scotty", "git", httpLinks());
  }

  private Repository createWorf() {
    return new Repository("clingon", "worf", "git", httpLinks());
  }

  private Repository createVader() {
    return new Repository("deathstar", "vader", "git", httpLinks());
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

  void mockEmptyIndex() {
    Index index = new Index();
    ScmManagerApiTestMocks.mockResult(when(api.index()), index);
  }

  void mockCorrectIndexForVersion(String version) {
    Index index = spy(new Index(linkingTo().single(link("login", "http://example.com/")).build()));
    ScmManagerApiTestMocks.mockResult(when(api.index()), index);
    when(index.getVersion()).thenReturn(version);
  }

  private void addRepositories(Repository... repositories) {
    ScmManagerApiTestMocks.mockResult(when(api.getNamespaces()), Arrays.stream(repositories).map(r -> new Namespace(r.getNamespace())).toList());
    ScmManagerApiTestMocks.mockResult(when(api.getRepositories(any(ScmManagerApi.SearchQuery.class))), asList(repositories));
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
