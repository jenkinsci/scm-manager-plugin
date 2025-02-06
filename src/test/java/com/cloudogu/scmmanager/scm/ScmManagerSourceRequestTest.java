package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerObservable;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmManagerSourceRequestTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private ScmManagerSource source;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private ScmManagerSourceContext context;

    private Set<SCMHead> heads;
    private ScmManagerPullRequestHead pr42;
    private ScmManagerPullRequestHead pr21;

    @Before
    public void prepareHeads() {
        heads = new HashSet<>();

        ScmManagerHead develop = ScmTestData.branch("develop");
        heads.add(develop);
        ScmManagerHead main = ScmTestData.branch("main");
        heads.add(main);
        pr42 = ScmTestData.pullRequest("42", main, develop);
        heads.add(pr42);
        pr21 = ScmTestData.pullRequest("21", develop, main);
        heads.add(pr21);
        heads.add(ScmTestData.tag("1.0.0"));
    }

    @Test
    public void shouldCollectPullRequests() {
        SCMHeadObserver observer = mock(SCMHeadObserver.class);
        when(observer.getIncludes()).thenReturn(heads);
        when(context.observer()).thenReturn(observer);

        ScmManagerSourceRequest request = new ScmManagerSourceRequest(source, context, null);
        assertThat(request.getPullRequests()).containsOnly(pr42, pr21);
    }

    @Test
    public void shouldCollectPullRequestWhenPreparingForFullScan() {
        Set<ScmManagerObservable> observables = heads.stream().map(TestingObservable::new).collect(Collectors.toSet());

        ScmManagerSourceRequest request = new ScmManagerSourceRequest(source, context, null);
        request.prepareForFullScan(observables);
        assertThat(request.getPullRequests()).containsOnly(pr42, pr21);
    }

    public static class TestingObservable implements ScmManagerObservable {

        private final SCMHead head;

        public TestingObservable(SCMHead head) {
            this.head = head;
        }

        @Override
        public SCMHead head() {
            return head;
        }

        @Override
        public SCMRevision revision() {
            return null;
        }
    }

}
