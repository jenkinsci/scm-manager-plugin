package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestRevision;
import com.cloudogu.scmmanager.scm.api.ScmManagerTag;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.plugins.git.UserMergeOptions;
import hudson.plugins.git.extensions.impl.PreBuildMerge;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.scm.api.SCMRevision;
import org.jenkinsci.plugins.gitclient.MergeCommand;

import java.util.Arrays;

public class ScmManagerGitSCMBuilder extends GitSCMBuilder<ScmManagerGitSCMBuilder> {

  public ScmManagerGitSCMBuilder(@NonNull LinkBuilder linkBuilder, @NonNull ScmManagerHead head, SCMRevision revision, String credentialsId) {
    super(head, revision, head.getCloneInformation().getUrl(), credentialsId);
    // clean up
    withoutRefSpecs();

    withBrowser(new ScmManagerGitRepositoryBrowser(linkBuilder));

    if (head instanceof ScmManagerTag) {
      withRefSpec("+refs/tags/" + head.getName() + ":refs/tags/" + head.getName());
    } else if (head instanceof ScmManagerPullRequestHead) {
      ScmManagerPullRequestHead prHead = (ScmManagerPullRequestHead) head;

      ScmManagerHead source = prHead.getSource();
      withHead(source);

      ScmManagerHead target = prHead.getTarget();

      withRefSpecs(Arrays.asList(
        "+refs/heads/" + source.getName() + ":refs/remotes/origin/" + source.getName(),
        "+refs/heads/" + target.getName() + ":refs/remotes/origin/" + target.getName()
      ));

      // revision is null on initial build
      if (revision != null) {
        ScmManagerPullRequestRevision prRevision = (ScmManagerPullRequestRevision) revision;
        withRevision(prRevision.getSourceRevision());
      }

      withExtension(new PreBuildMerge(
        new UserMergeOptions(
          // remote name is set by GitSCMBuilder constructor
          "origin",
          target.getName(),
          "resolve",
          MergeCommand.GitPluginFastForwardMode.NO_FF
        )
      ));
    } else {
      withRefSpec("+refs/heads/" + head.getName() + ":refs/remotes/@{remote}/" + head.getName());
    }
  }
}
