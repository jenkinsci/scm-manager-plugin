package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMSourceRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestDiscoveryTraitTest {

    @Mock
    private ScmManagerSourceContext context;

    @Test
    public void shouldDecorateContextWithFilter() {
        PullRequestDiscoveryTrait trait = new PullRequestDiscoveryTrait(true);
        trait.decorateContext(context);

        verify(context).wantPullRequests(true);
        verify(context).withFilter(any(PullRequestDiscoveryTrait.ExcludePullRequestBranchHeadFilter.class));
    }

    @Test
    public void shouldNotDecorateContextWithFilter() {
        PullRequestDiscoveryTrait trait = new PullRequestDiscoveryTrait(false);
        trait.decorateContext(context);

        verify(context).wantPullRequests(true);
        verify(context, never()).withFilter(any(PullRequestDiscoveryTrait.ExcludePullRequestBranchHeadFilter.class));
    }

    @Test
    public void shouldNotExcludeNonScmManagerRequest() {
        SCMSourceRequest request = mock(SCMSourceRequest.class);
        SCMHead head = ScmTestData.branch("main");

        assertThat(isExcluded(request, head)).isFalse();
    }

    @Test
    public void shouldNotExcludeNonScmManagerHeads() {
        SCMSourceRequest request = mock(ScmManagerSourceRequest.class);
        SCMHead head = mock(SCMHead.class);

        assertThat(isExcluded(request, head)).isFalse();
    }

    @Test
    public void shouldNotExcludeTags() {
        SCMSourceRequest request = mock(ScmManagerSourceRequest.class);
        ScmManagerHead tag = ScmTestData.tag("1.0.0");

        assertThat(isExcluded(request, tag)).isFalse();
    }

    @Test
    public void shouldNotExcludePullRequests() {
        SCMSourceRequest request = mock(ScmManagerSourceRequest.class);
        ScmManagerHead source = ScmTestData.branch("feature/awesome");
        ScmManagerHead target = ScmTestData.branch("develop");
        ScmManagerPullRequestHead pullRequest = ScmTestData.pullRequest("42", target, source);

        assertThat(isExcluded(request, pullRequest)).isFalse();
    }

    @Test
    public void shouldExcludeBranchWithOpenPR() {
        ScmManagerSourceRequest request = mock(ScmManagerSourceRequest.class);
        ScmManagerHead source = ScmTestData.branch("feature/awesome");
        ScmManagerHead target = ScmTestData.branch("develop");
        ScmManagerPullRequestHead pullRequest = ScmTestData.pullRequest("42", target, source);

        when(request.getPullRequests()).thenReturn(Collections.singletonList(pullRequest));

        assertThat(isExcluded(request, source)).isTrue();
    }

    private boolean isExcluded(@NonNull SCMSourceRequest request, @NonNull SCMHead head) {
        return new PullRequestDiscoveryTrait.ExcludePullRequestBranchHeadFilter().isExcluded(request, head);
    }
}
