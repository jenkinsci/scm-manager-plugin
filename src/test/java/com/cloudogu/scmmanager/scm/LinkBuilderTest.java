package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import org.junit.Test;

import static com.cloudogu.scmmanager.scm.ScmTestData.*;
import static org.assertj.core.api.Assertions.assertThat;


public class LinkBuilderTest {

    private static final String PREFIX = "https://scm.hitchhiker.com/scm/repo/spaceships/heartOfGold";

    private final LinkBuilder builder = new LinkBuilder("https://scm.hitchhiker.com/scm", "spaceships", "heartOfGold");

    @Test
    public void shouldAddSlash() {
        assertThat(builder.concat("a", "b")).isEqualTo("a/b");
    }

    @Test
    public void shouldAppendMultiple() {
        assertThat(builder.concat("a", "b", "c", "d")).isEqualTo("a/b/c/d");
    }

    @Test
    public void shouldAvoidDoubleSlash() {
        assertThat(builder.concat("a/", "b")).isEqualTo("a/b");
        assertThat(builder.concat("a", "/b")).isEqualTo("a/b");
        assertThat(builder.concat("a/", "/b")).isEqualTo("a/b");
    }

    @Test
    public void shouldCreateBranchLink() {
        String link = builder.create(branch("develop"));
        assertThat(link).isEqualTo(PREFIX + "/code/sources/develop");
    }

    @Test
    public void shouldEscapeBranchNameInLink() {
        String link = builder.create(branch("feature/some/nice/one"));
        assertThat(link).isEqualTo(PREFIX + "/code/sources/feature%2Fsome%2Fnice%2Fone");
    }

    @Test
    public void shouldCreateTagLink() {
        String link = builder.create(tag("42.0"));
        assertThat(link).isEqualTo(PREFIX + "/code/sources/42.0");
    }

    @Test
    public void shouldCreatePullRequestLink() {
        String link = builder.create(pullRequest("42", branch("main"), branch("develop")));
        assertThat(link).isEqualTo(PREFIX + "/pull-request/42");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForUnknownHead() {
        builder.create(new SCMHead("unknown"));
    }

    @Test
    public void shouldReturnRepoUrl() {
        assertThat(builder.repo()).isEqualTo(PREFIX);
    }

    @Test
    public void shouldReturnRevisionSourcesLink() {
        ScmManagerRevision revision = revision(branch("develop"), "1adb99ba6c7ca1fe5ffc8c4fe5feb809bf0d26c5");
        String link = builder.create(revision);
        assertThat(link).isEqualTo(PREFIX + "/code/sources/1adb99ba6c7ca1fe5ffc8c4fe5feb809bf0d26c5");
    }

    @Test
    public void shouldReturnSourceRevisionSourcesLink() {
        ScmManagerPullRequestHead head = pullRequest("42", branch("main"), branch("develop"));
        ScmManagerPullRequestRevision revision = pullRequestRevision(head, "abc21", "cde42");
        String link = builder.create(revision);
        assertThat(link).isEqualTo(PREFIX + "/code/sources/cde42");
    }

    @Test
    public void shouldReturnChangesetLink() {
        String link = builder.changeset("cde42");
        assertThat(link).isEqualTo(PREFIX + "/code/changeset/cde42");
    }

    @Test
    public void shouldReturnDiffLink() {
        String link = builder.diff("cde42", "a/b/c");
        assertThat(link).isEqualTo(PREFIX + "/code/changeset/cde42#diff-a/b/c");
    }

    @Test
    public void shouldReturnSourceLink() {
        String link = builder.source("cde42", "a/b/c");
        assertThat(link).isEqualTo(PREFIX + "/code/sources/cde42/a/b/c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForUnknownRevision() {
        builder.create(new SCMRevision(branch("develop")) {
            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }
        });
    }


}
