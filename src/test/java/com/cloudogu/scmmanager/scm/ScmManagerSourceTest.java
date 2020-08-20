package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.scm.api.Authentications;
import com.cloudogu.scmmanager.scm.api.Branch;
import com.cloudogu.scmmanager.scm.api.Changeset;
import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.PullRequest;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerTag;
import com.cloudogu.scmmanager.scm.api.Tag;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Action;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.util.StreamTaskListener;
import jenkins.scm.api.SCMEvent;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.trait.SCMSourceRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.cloudogu.scmmanager.scm.ScmTestData.branch;
import static com.cloudogu.scmmanager.scm.ScmTestData.revision;
import static java.util.Arrays.asList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ScmManagerSourceTest {

  public static final CloneInformation CLONE_INFORMATION = new CloneInformation("git", "https://hitchhiker.com/scm");
  public static final Repository REPOSITORY = new Repository("space", "X", "git");

  @Mock
  private SCMSourceOwner scmSourceOwner;
  @Mock
  private Authentications mockedAuthentication;
  @Mock
  private HttpAuthentication authentication;
  @Mock
  private Function<SCMSourceOwner, Authentications> authenticationsProvider;

  @Captor
  private ArgumentCaptor<SCMHead> head;
  @Captor
  private ArgumentCaptor<SCMRevision> revision;

  @Mock
  private SCMHeadObserver observer;

  @Mock
  private ScmManagerSourceRequest request;

  @Spy
  private BiFunction<String, HttpAuthentication, ScmManagerApi> apiClientFactory;

  @Mock
  private SCMSourceCriteria criteria;

  @Mock
  private TaskListener listener;

  @Mock
  private ScmManagerApi api;
  private ScmManagerSource source;

  @Before
  public void initMocks() throws IOException, InterruptedException {
    when(apiClientFactory.apply("http://hithchiker.com/scm", authentication)).thenReturn(api);
    when(authenticationsProvider.apply(scmSourceOwner)).thenReturn(mockedAuthentication);
    when(mockedAuthentication.from("http://hithchiker.com/scm", "dent")).thenReturn(authentication);
    source = new ScmManagerSource("http://hithchiker.com/scm", "space/X/git", "dent", apiClientFactory, authenticationsProvider);
    source.setOwner(scmSourceOwner);
    captureProcess();
  }

  private void captureProcess() throws IOException, InterruptedException {
    when(
      request.process(
        head.capture(),
        revision.capture(),
        any(SCMSourceRequest.ProbeLambda.class),
        any(SCMSourceRequest.Witness.class)
      )
    ).thenReturn(false);
  }

  @Test
  public void shouldUseConfiguredValues() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(completedFuture(REPOSITORY));

    source.retrieve(criteria, observer, null, listener);

    verify(apiClientFactory).apply("http://hithchiker.com/scm", authentication);
  }

  @Test
  public void shouldObserverBranches() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(completedFuture(REPOSITORY));
    when(api.getBranches(REPOSITORY)).thenReturn(completedFuture(asList(new Branch("feature/hog", "42"))));
    when(request.isFetchBranches()).thenReturn(true);

    source.handleRequest(observer, null, request);

    assertThat(head.getValue().getName()).isEqualTo("feature/hog");
    assertThat(revision.getValue()).isInstanceOf(ScmManagerRevision.class).extracting("revision").isEqualTo("42");
  }

  @Test
  public void shouldObserverPullRequests() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(completedFuture(REPOSITORY));
    when(api.getPullRequests(REPOSITORY)).thenReturn(completedFuture(asList(createPullRequest())));
    when(request.isFetchPullRequests()).thenReturn(true);

    source.handleRequest(observer, null, request);

    assertThat(head.getValue().getName()).isEqualTo("PR-42");
  }

  @Test
  public void shouldProcessTagRequests() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(completedFuture(REPOSITORY));
    when(api.getTags(REPOSITORY)).thenReturn(completedFuture(asList(createTag())));
    when(request.isFetchTags()).thenReturn(true);

    source.handleRequest(observer, null, request);

    assertThat(head.getValue().getName()).isEqualTo("4.2");
  }

  @Test
  public void shouldObserveOnlyChangedBranch() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(completedFuture(REPOSITORY));
    when(api.getBranch(REPOSITORY, "develop")).thenReturn(completedFuture(new Branch("develop", "42")));
    when(request.isFetchBranches()).thenReturn(true);
    when(observer.getIncludes()).thenReturn(Collections.singleton(new ScmManagerHead(CLONE_INFORMATION, "develop")));

    source.handleRequest(observer, null, request);

    assertThat(head.getValue().getName()).isEqualTo("develop");
    verify(api, never()).getBranches(REPOSITORY);
  }

  @Test
  public void shouldDoNothingIfBranchChangesWithoutRequestingThem() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(completedFuture(REPOSITORY));
    when(observer.getIncludes()).thenReturn(Collections.singleton(new ScmManagerHead(CLONE_INFORMATION, "develop")));

    source.handleRequest(observer, null, request);

    verifyThatNothingIsProcessed();
    verify(api, never()).getBranches(REPOSITORY);
  }

  private void verifyThatNothingIsProcessed() throws IOException, InterruptedException {
    verify(request, never()).process(
      any(SCMHead.class),
      any(SCMRevision.class),
      any(SCMSourceRequest.ProbeLambda.class),
      any(SCMSourceRequest.Witness.class)
    );
  }

  @Test
  public void shouldObserveOnlyChangedTag() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(completedFuture(REPOSITORY));
    when(api.getTag(REPOSITORY, "4.2")).thenReturn(completedFuture(createTag()));
    when(observer.getIncludes()).thenReturn(Collections.singleton(new ScmManagerTag(CLONE_INFORMATION, "4.2", 0L)));
    when(request.isFetchTags()).thenReturn(true);

    source.handleRequest(observer, null, request);

    assertThat(head.getValue().getName()).isEqualTo("4.2");
    verify(api, never()).getTags(REPOSITORY);
  }

  @Test
  public void shouldDoNothingIfTagChangesWithoutRequestingThem() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(completedFuture(REPOSITORY));
    when(observer.getIncludes()).thenReturn(Collections.singleton(new ScmManagerTag(CLONE_INFORMATION, "4.2", 0L)));

    source.handleRequest(observer, null, request);

    verifyThatNothingIsProcessed();
    verify(api, never()).getTags(REPOSITORY);
  }

  private Tag createTag() {
    return new Tag("4.2", "42", new Changeset("42", new Date(0L)), CLONE_INFORMATION);
  }

  @Test
  public void shouldObserveOnlyChangedPullRequest() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(completedFuture(REPOSITORY));
    when(api.getPullRequest(REPOSITORY, "42")).thenReturn(completedFuture(createPullRequest()));
    when(observer.getIncludes()).thenReturn(Collections.singleton(
      new ScmManagerPullRequestHead(
        CLONE_INFORMATION,
        "42",
        new ScmManagerHead(CLONE_INFORMATION, "main"),
        new ScmManagerHead(CLONE_INFORMATION, "develop")
      )
    ));
    when(request.isFetchPullRequests()).thenReturn(true);

    source.handleRequest(observer, null, request);

    assertThat(head.getValue().getName()).isEqualTo("PR-42");
    verify(api, never()).getPullRequests(REPOSITORY);
  }

  private PullRequest createPullRequest() {
    return new PullRequest(
      "42",
      new Branch("main", "21"),
      new Branch("develop", "42"),
      CLONE_INFORMATION
    );
  }

  @Test
  public void shouldDoNothingIfPullRequestChangesWithoutRequestingThem() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(completedFuture(REPOSITORY));
    when(observer.getIncludes()).thenReturn(Collections.singleton(
      new ScmManagerPullRequestHead(
        CLONE_INFORMATION,
        "42",
        new ScmManagerHead(CLONE_INFORMATION, "main"),
        new ScmManagerHead(CLONE_INFORMATION, "develop")
      )
    ));

    source.handleRequest(observer, null, request);

    verifyThatNothingIsProcessed();
    verify(api, never()).getPullRequests(REPOSITORY);
  }

  @Test
  public void shouldObserveAllOnRemove() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(completedFuture(REPOSITORY));
    when(api.getBranches(REPOSITORY)).thenReturn(completedFuture(asList(new Branch("feature/hog", "42"))));
    when(request.isFetchBranches()).thenReturn(true);

    SCMHeadEvent<?> event = mock(SCMHeadEvent.class);
    when(event.getType()).thenReturn(SCMEvent.Type.REMOVED);

    source.handleRequest(observer, event, request);

    assertThat(head.getValue().getName()).isEqualTo("feature/hog");
    assertThat(revision.getValue()).isInstanceOf(ScmManagerRevision.class).extracting("revision").isEqualTo("42");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionForNonScmManagerHeads() {
    source.build(new SCMHead("throw-it"));
  }

  @Test
  public void shouldRetrieveScmManagerRepoLink() {
    List<Action> actions = source.retrieveActions(null, listener());
    assertScmManagerLink(actions);
  }

  @Test
  public void shouldRetrieveScmManagerHeadLink() {
    List<Action> actions = source.retrieveActions(branch("develop"), null, listener());
    assertScmManagerLink(actions);
  }

  @Test
  public void shouldRetrieveScmManagerRevisionLink() {
    List<Action> actions = source.retrieveActions(revision(branch("develop"), "abc42"), null, listener());
    assertScmManagerLink(actions);
  }

  private void assertScmManagerLink(List<Action> actions) {
    assertThat(actions).hasSize(1);
    Action action = actions.get(0);
    assertThat(action).isInstanceOf(ScmManagerLink.class);
  }

  @NonNull
  private StreamTaskListener listener() {
    return new StreamTaskListener(System.out, StandardCharsets.UTF_8);
  }
}
