package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerRevision;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;

public class LinkBuilder {

  private final String url;

  public LinkBuilder(String serverUrl, String namespace, String name) {
    this.url = concat(serverUrl, "repo", namespace, name);
  }

  @VisibleForTesting
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
      return concat(url, "code/sources", head.getName());
    } else {
      throw new IllegalArgumentException("unknown type of head " + head);
    }
  }

  public String create(@NonNull SCMRevision revision) {
    if (revision instanceof ScmManagerPullRequestRevision) {
      return concat(url, "code/changeset/", ((ScmManagerPullRequestRevision) revision).getSourceRevision().getRevision());
    } else if (revision instanceof ScmManagerRevision) {
      return concat(url, "code/changeset/", ((ScmManagerRevision) revision).getRevision());
    } else {
      throw new IllegalArgumentException("unknown type of revision " + revision);
    }
  }

}
