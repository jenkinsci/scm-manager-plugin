package com.cloudogu.scmmanager.scm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collections;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMSourceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PullRequestDiscoveryTraitTest {

    @Mock
    private ScmManagerSourceContext context;

    @Test
    void shouldDecorateContextWithFilter() {
        PullRequestDiscoveryTrait trait = new PullRequestDiscoveryTrait(true);
        trait.decorateContext(context);

        verify(context).wantPullRequests(true);
        verify(context).withFilter(any(PullRequestDiscoveryTrait.ExcludePullRequestBranchHeadFilter.class));
    }

    @Test
    void shouldNotDecorateContextWithFilter() {
        PullRequestDiscoveryTrait trait = new PullRequestDiscoveryTrait(false);
        trait.decorateContext(context);

        verify(context).wantPullRequests(true);
        verify(context, never()).withFilter(any(PullRequestDiscoveryTrait.ExcludePullRequestBranchHeadFilter.class));
    }

    @Test
    void shouldNotExcludeNonScmManagerRequest() {
        SCMSourceRequest request = mock(SCMSourceRequest.class);
        SCMHead head = ScmTestData.branch("main");

        assertThat(isExcluded(request, head)).isFalse();
    }

    @Test
    void shouldNotExcludeNonScmManagerHeads() {
        SCMSourceRequest request = mock(ScmManagerSourceRequest.class);
        SCMHead head = mock(SCMHead.class);

        assertThat(isExcluded(request, head)).isFalse();
    }

    @Test
    void shouldNotExcludeTags() {
        SCMSourceRequest request = mock(ScmManagerSourceRequest.class);
        ScmManagerHead tag = ScmTestData.tag("1.0.0");

        assertThat(isExcluded(request, tag)).isFalse();
    }

    @Test
    void shouldNotExcludePullRequests() {
        SCMSourceRequest request = mock(ScmManagerSourceRequest.class);
        ScmManagerHead source = ScmTestData.branch("feature/awesome");
        ScmManagerHead target = ScmTestData.branch("develop");
        ScmManagerPullRequestHead pullRequest = ScmTestData.pullRequest("42", target, source);

        assertThat(isExcluded(request, pullRequest)).isFalse();
    }

    @Test
    void shouldExcludeBranchWithOpenPR() {
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
