package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.HttpAuthentication;
import com.cloudogu.scmmanager.scm.api.ApiClient.Promise;
import com.cloudogu.scmmanager.scm.api.Authentications;
import com.cloudogu.scmmanager.scm.api.Branch;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSourceOwner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmManagerSourceTest {

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
  private ScmManagerApi api;
  private ScmManagerSource source;

  @Before
  public void initMocks() throws IOException, InterruptedException {
    when(apiClientFactory.apply("http://hithchiker.com/scm", authentication)).thenReturn(api);
    when(authenticationsProvider.apply(scmSourceOwner)).thenReturn(mockedAuthentication);
    when(mockedAuthentication.from("http://hithchiker.com/scm", "dent")).thenReturn(authentication);
    source = new ScmManagerSource("http://hithchiker.com/scm", "space/X/git", "dent", apiClientFactory, authenticationsProvider);
    source.setOwner(scmSourceOwner);
    doNothing().when(observer).observe(head.capture(), revision.capture());
  }

  @Test
  public void shouldUserConfiguredValues() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(new Promise<>(REPOSITORY));

    source.observe(observer, request);

    verify(apiClientFactory).apply("http://hithchiker.com/scm", authentication);
  }

  @Test
  public void shouldObserverBranches() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(new Promise<>(REPOSITORY));
    when(api.getBranches(REPOSITORY)).thenReturn(new Promise<>(asList(new Branch("feature/hog", "42"))));
    when(request.isFetchBranches()).thenReturn(true);

    source.observe(observer, request);

    assertThat(head.getValue().getName()).isEqualTo("feature/hog");
    assertThat(revision.getValue()).isInstanceOf(ScmManagerRevision.class).extracting("revision").isEqualTo("42");
  }

  @Test
  public void shouldObserverPullRequests() throws IOException, InterruptedException {
    when(api.getRepository("space", "X")).thenReturn(new Promise<>(REPOSITORY));
    when(api.getBranches(REPOSITORY)).thenReturn(new Promise<>(asList(new Branch("feature/hog", "42"))));
    when(request.isFetchBranches()).thenReturn(true);

    source.observe(observer, request);

    assertThat(head.getValue().getName()).isEqualTo("feature/hog");
    assertThat(revision.getValue()).isInstanceOf(ScmManagerRevision.class).extracting("revision").isEqualTo("42");
  }
}
