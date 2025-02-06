package com.cloudogu.scmmanager.scm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import com.cloudogu.scmmanager.scm.api.ScmManagerApiFactory;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceObserver;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScmManagerNavigatorTest {

    private static final String SERVER_URL = "https://hitchhiker.com";
    private static final String NAMESPACE = "spaceships";
    private static final String CRENDETIALS = "zaphod";

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Mock
    private ScmManagerApiFactory apiFactory;

    @Mock
    private ScmManagerApi api;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SCMSourceObserver observer;

    @Test
    public void shouldObserveGitRepositories() throws IOException, InterruptedException {
        mockApiResponse(repository("git", "heart-of-gold"));

        ScmManagerNavigator navigator = navigator("git");
        navigator.visitSources(observer);

        verify(observer).observe("heart-of-gold");
    }

    @Test
    public void shouldObserveRepositoriesForAllNamespaces() throws IOException, InterruptedException {
        mockApiResponse(repository("git", "heart-of-gold"));

        ScmManagerNavigator navigator = navigatorForCustomNamespace(ScmManagerNavigator.ALL_NAMESPACES_LABEL, "git");
        navigator.visitSources(observer);

        verify(observer).observe("spaceships/heart-of-gold");
    }

    @Test
    public void shouldObserveMercurialRepositories() throws IOException, InterruptedException {
        mockApiResponse(repository("hg", "firefly"));

        ScmManagerNavigator navigator = navigator("mercurial");
        navigator.visitSources(observer);

        verify(observer).observe("firefly");
    }

    @Test
    public void shouldNotObserveNonSupportedRepositories() throws IOException, InterruptedException {
        mockApiResponse(repository("git", "heart-of-gold"), repository("hg", "firefly"));

        ScmManagerNavigator navigator = navigator("subversion");
        navigator.visitSources(observer);

        verify(observer, never()).observe(anyString());
    }

    @Test
    public void shouldNotObserveSubversionWithoutTrait() throws IOException, InterruptedException {
        mockApiResponse(repository("svn", "elysium"));

        ScmManagerNavigator navigator = navigator("subversion");
        navigator.visitSources(observer);

        verify(observer, never()).observe("elysium");
    }

    @Test
    public void shouldObserveSubversion() throws IOException, InterruptedException {
        mockApiResponse(repository("svn", "elysium"));

        ScmManagerNavigator navigator = navigator("subversion");
        navigator.setTraits(Collections.singletonList(new ScmManagerSvnNavigatorTrait()));
        navigator.visitSources(observer);

        verify(observer).observe("elysium");
    }

    @Test
    public void shouldCreateSvnSourceAndPassIncludesAndExcludes() throws IOException, InterruptedException {
        mockApiResponse(repository("svn", "elysium"));

        SCMSourceObserver.ProjectObserver projectObserver = mock(SCMSourceObserver.ProjectObserver.class);
        when(observer.observe("elysium")).thenReturn(projectObserver);

        ScmManagerNavigator navigator = navigator("subversion");
        navigator.setTraits(Collections.singletonList(new ScmManagerSvnNavigatorTrait("trunk", "*/out")));
        navigator.visitSources(observer);

        ArgumentCaptor<SCMSource> sourceCaptor = ArgumentCaptor.forClass(SCMSource.class);
        verify(projectObserver).addSource(sourceCaptor.capture());

        SCMSource source = sourceCaptor.getValue();
        assertThat(source).isInstanceOf(ScmManagerSvnSource.class);

        ScmManagerSvnSource svnSource = (ScmManagerSvnSource) source;
        assertThat(svnSource.getIncludes()).isEqualTo("trunk");
        assertThat(svnSource.getExcludes()).isEqualTo("*/out");
    }

    @Test
    public void shouldCreateSource() throws IOException, InterruptedException {
        mockApiResponse(repository("git", "heart-of-gold"));

        SCMSourceObserver.ProjectObserver projectObserver = mock(SCMSourceObserver.ProjectObserver.class);
        when(observer.observe("heart-of-gold")).thenReturn(projectObserver);

        ScmManagerNavigator navigator = navigator("git");
        navigator.visitSources(observer);

        ArgumentCaptor<SCMSource> sourceCaptor = ArgumentCaptor.forClass(SCMSource.class);
        verify(projectObserver).addSource(sourceCaptor.capture());

        SCMSource source = sourceCaptor.getValue();
        assertThat(source).isInstanceOf(ScmManagerSource.class);
    }

    @Test(expected = IOException.class)
    public void shouldThrowIOExceptionOnError() throws IOException, InterruptedException, ExecutionException {
        mockApiResponse(repository("git", "heart-of-gold"));

        CompletableFuture<List<Repository>> repositoryRequest = mock(CompletableFuture.class);
        when(api.getRepositories(NAMESPACE)).thenReturn(repositoryRequest);
        when(repositoryRequest.get()).thenThrow(new ExecutionException("test", null));

        ScmManagerNavigator navigator = navigator("git");
        navigator.visitSources(observer);
    }

    @NonNull
    private ScmManagerNavigator navigator(String... installedPlugins) {
        return navigatorForCustomNamespace(NAMESPACE, installedPlugins);
    }

    @NonNull
    private ScmManagerNavigator navigatorForCustomNamespace(String namespace, String... installedPlugins) {
        return new ScmManagerNavigator(
                "scm", SERVER_URL, namespace, CRENDETIALS, dependencyChecker(installedPlugins), apiFactory);
    }

    private Predicate<String> dependencyChecker(String... plugins) {
        List<String> pluginList = Arrays.asList(plugins);
        return pluginList::contains;
    }

    private Repository repository(String type, String name) {
        Links links = Links.linkingTo()
                .array(Link.linkBuilder("protocol", "http://scm.hitchhiker.com")
                        .withName("http")
                        .build())
                .build();
        return new Repository(NAMESPACE, name, type, links);
    }

    private void mockApiResponse(Repository... repositories) {
        when(apiFactory.create(observer.getContext(), SERVER_URL, CRENDETIALS)).thenReturn(api);
        when(observer.getIncludes()).thenReturn(null);
        lenient()
                .when(api.getRepositories(NAMESPACE))
                .thenReturn(CompletableFuture.completedFuture(Arrays.asList(repositories)));
        lenient()
                .when(api.getRepositories())
                .thenReturn(CompletableFuture.completedFuture(Arrays.asList(repositories)));
    }
}
