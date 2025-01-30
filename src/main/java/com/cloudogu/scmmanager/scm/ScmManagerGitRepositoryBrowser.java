package com.cloudogu.scmmanager.scm;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import net.sf.json.JSONObject;
import org.eclipse.jgit.util.QuotedString;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.net.URL;

public class ScmManagerGitRepositoryBrowser extends GitRepositoryBrowser  {

  private static final long serialVersionUID = 1L;

  private final LinkBuilder linkBuilder;

  /**
   * This constructor is used if the browser is used within a normal git source (non SCM-Manager Source).
   *
   * @param repoUrl repository url
   */
  @DataBoundConstructor
  public ScmManagerGitRepositoryBrowser(String repoUrl) {
    this(new LinkBuilder(repoUrl));
  }

  /**
   * This constructor is used directly from {@link ScmManagerGitSCMBuilder}.
   *
   * @param linkBuilder link builder
   */
  ScmManagerGitRepositoryBrowser(LinkBuilder linkBuilder) {
    super(linkBuilder.repo());
    this.linkBuilder = linkBuilder;
  }

  @Override
  public URL getChangeSetLink(GitChangeSet changeSet) throws IOException {
    return new URL(linkBuilder.changeset(changeSet.getId()));
  }

  @Override
  public URL getDiffLink(GitChangeSet.Path path) throws IOException {
    if (path.getEditType() != EditType.EDIT || path.getSrc() == null || path.getDst() == null || path.getChangeSet().getParentCommit() == null) {
      return null;
    }
    return new URL(linkBuilder.diff(path.getChangeSet().getId(), dequote(path)));
  }

  private String dequote(GitChangeSet.Path path) {
    String p = path.getPath();
    if (p != null && p.startsWith("\"")) {
      return QuotedString.GIT_PATH.dequote(p);
    }
    return p;
  }

  @Override
  public URL getFileLink(GitChangeSet.Path path) throws IOException {
    // we have no source link for deleted files, we return diff link instead
    if (path.getEditType().equals(EditType.DELETE)) {
      return new URL(linkBuilder.diff(path.getChangeSet().getId(), dequote(path)));
    }
    return new URL(linkBuilder.source(path.getChangeSet().getId(), dequote(path)));
  }

  @Extension(optional = true)
  public static class ScmManagerGitRepositoryBrowserDescriptor extends Descriptor<RepositoryBrowser<?>> {

    @NonNull
    @Override
    public String getDisplayName() {
      return "SCM-Manager";
    }

    @Override
    public ScmManagerGitRepositoryBrowser newInstance(StaplerRequest req, @NonNull JSONObject jsonObject) throws FormException {
      assert req != null; //see inherited javadoc
      return req.bindJSON(ScmManagerGitRepositoryBrowser.class, jsonObject);
    }

  }

}
