package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;

public class LinkBuilder implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String url;

    public LinkBuilder(String url) {
        this.url = url;
    }

    public LinkBuilder(String serverUrl, String namespace, String name) {
        this.url = concat(serverUrl, "repo", namespace, name);
    }

    @VisibleForTesting
    String concat(String base, String... parts) {
        StringBuilder link = new StringBuilder(base);
        for (String part : parts) {
            if (!endsWith(link, '/') && !part.startsWith("/")) {
                link.append("/").append(part);
            } else if (endsWith(link, '/') && part.startsWith("/")) {
                link.append(part.substring(1));
            } else {
                link.append(part);
            }
        }
        return link.toString();
    }

    private boolean endsWith(CharSequence sequence, char c) {
        return sequence.charAt(sequence.length() - 1) == c;
    }

    public String repo() {
        return url;
    }

    public String create(@NonNull SCMHead head) {
        if (head instanceof ScmManagerPullRequestHead) {
            return concat(url, "pull-request", ((ScmManagerPullRequestHead) head).getId());
        } else if (head instanceof ScmManagerHead) {
            String encode = encodeForUrl(head);
            return sources(encode);
        } else {
            throw new IllegalArgumentException("unknown type of head " + head);
        }
    }

    @SuppressWarnings("java:S112") // The exception caught here can never be thrown
    private String encodeForUrl(SCMHead head) {
        try {
            return URLEncoder.encode(head.getName(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // this should not happen because we use StandardCharsets
        }
    }

    public String create(@NonNull SCMRevision revision) {
        if (revision instanceof ScmManagerPullRequestRevision) {
            return sources(((ScmManagerPullRequestRevision) revision)
                    .getSourceRevision()
                    .getRevision());
        } else if (revision instanceof ScmManagerRevision) {
            return sources(((ScmManagerRevision) revision).getRevision());
        } else {
            throw new IllegalArgumentException("unknown type of revision " + revision);
        }
    }

    public String changeset(String revision) {
        return concat(url, "code/changeset", revision);
    }

    public String diff(String revision, String path) {
        return changeset(revision) + "#diff-" + path;
    }

    public String source(String revision, String path) {
        return concat(sources(revision), path);
    }

    private String sources(String revision) {
        return concat(url, "code/sources", revision);
    }
}
