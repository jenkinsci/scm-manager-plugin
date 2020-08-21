package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;

import java.io.Serializable;

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
  @SuppressWarnings("java:S1643") // we concat only up to 4 items together this is ok even in a for loop
  String concat(String base, String... parts) {
    String link = base;
    for (String part : parts) {
      if (!link.endsWith("/") && !part.startsWith("/")) {
        link += "/" + part;
      } else if (link.endsWith("/") && part.startsWith("/")) {
        link += part.substring(1);
      } else {
        link += part;
      }
    }
    return link;
  }

  public String repo() {
    return url;
  }

  public String create(@NonNull SCMHead head) {
    if (head instanceof ScmManagerPullRequestHead) {
      return concat(url, "pull-request", ((ScmManagerPullRequestHead) head).getId());
    } else if (head instanceof ScmManagerHead) {
      return sources(head.getName());
    } else {
      throw new IllegalArgumentException("unknown type of head " + head);
    }
  }

  public String create(@NonNull SCMRevision revision) {
    if (revision instanceof ScmManagerPullRequestRevision) {
      return sources(((ScmManagerPullRequestRevision) revision).getSourceRevision().getRevision());
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
