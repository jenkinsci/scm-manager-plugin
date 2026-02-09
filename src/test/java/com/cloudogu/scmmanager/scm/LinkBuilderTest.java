package com.cloudogu.scmmanager.scm;

import static com.cloudogu.scmmanager.scm.ScmTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import org.junit.jupiter.api.Test;

class LinkBuilderTest {

    private static final String PREFIX = "https://scm.hitchhiker.com/scm/repo/spaceships/heartOfGold";

    private final LinkBuilder builder = new LinkBuilder("https://scm.hitchhiker.com/scm", "spaceships", "heartOfGold");

    @Test
    void shouldAddSlash() {
        assertThat(builder.concat("a", "b")).isEqualTo("a/b");
    }

    @Test
    void shouldAppendMultiple() {
        assertThat(builder.concat("a", "b", "c", "d")).isEqualTo("a/b/c/d");
    }

    @Test
    void shouldAvoidDoubleSlash() {
        assertThat(builder.concat("a/", "b")).isEqualTo("a/b");
        assertThat(builder.concat("a", "/b")).isEqualTo("a/b");
        assertThat(builder.concat("a/", "/b")).isEqualTo("a/b");
    }

    @Test
    void shouldCreateBranchLink() {
        String link = builder.create(branch("develop"));
        assertThat(link).isEqualTo(PREFIX + "/code/sources/develop");
    }

    @Test
    void shouldEscapeBranchNameInLink() {
        String link = builder.create(branch("feature/some/nice/one"));
        assertThat(link).isEqualTo(PREFIX + "/code/sources/feature%2Fsome%2Fnice%2Fone");
    }

    @Test
    void shouldCreateTagLink() {
        String link = builder.create(tag("42.0"));
        assertThat(link).isEqualTo(PREFIX + "/code/sources/42.0");
    }

    @Test
    void shouldCreatePullRequestLink() {
        String link = builder.create(pullRequest("42", branch("main"), branch("develop")));
        assertThat(link).isEqualTo(PREFIX + "/pull-request/42");
    }

    @Test
    void shouldFailForUnknownHead() {
        assertThrows(IllegalArgumentException.class, () -> builder.create(new SCMHead("unknown")));
    }

    @Test
    void shouldReturnRepoUrl() {
        assertThat(builder.repo()).isEqualTo(PREFIX);
    }

    @Test
    void shouldReturnRevisionSourcesLink() {
        ScmManagerRevision revision = revision(branch("develop"), "1adb99ba6c7ca1fe5ffc8c4fe5feb809bf0d26c5");
        String link = builder.create(revision);
        assertThat(link).isEqualTo(PREFIX + "/code/sources/1adb99ba6c7ca1fe5ffc8c4fe5feb809bf0d26c5");
    }

    @Test
    void shouldReturnSourceRevisionSourcesLink() {
        ScmManagerPullRequestHead head = pullRequest("42", branch("main"), branch("develop"));
        ScmManagerPullRequestRevision revision = pullRequestRevision(head, "abc21", "cde42");
        String link = builder.create(revision);
        assertThat(link).isEqualTo(PREFIX + "/code/sources/cde42");
    }

    @Test
    void shouldReturnChangesetLink() {
        String link = builder.changeset("cde42");
        assertThat(link).isEqualTo(PREFIX + "/code/changeset/cde42");
    }

    @Test
    void shouldReturnDiffLink() {
        String link = builder.diff("cde42", "a/b/c");
        assertThat(link).isEqualTo(PREFIX + "/code/changeset/cde42#diff-a/b/c");
    }

    @Test
    void shouldReturnSourceLink() {
        String link = builder.source("cde42", "a/b/c");
        assertThat(link).isEqualTo(PREFIX + "/code/sources/cde42/a/b/c");
    }

    @Test
    void shouldFailForUnknownRevision() {
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.create(new SCMRevision(branch("develop")) {
                    @Override
                    public boolean equals(Object obj) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }
                }));
    }
}
