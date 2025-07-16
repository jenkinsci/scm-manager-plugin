package com.cloudogu.scmmanager.update;

import com.cloudogu.scmmanager.scm.ScmManagerSource;
import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;

@Extension
public class UpdateScmMangerSourceWithApiData {

  @Initializer(after = InitMilestone.JOB_CONFIG_ADAPTED, before = InitMilestone.COMPLETED)
  public void resaveScmManagerSourceJobs() {
    Jenkins.get().getAllItems().forEach(item -> {
      if (item instanceof MultiBranchProject<?, ?> project && project.getSCMSources().stream().anyMatch(source -> source instanceof ScmManagerSource)) {
        //This triggers a rescan of the pipeline
        //And therefore the re-evaluation of its actions
        //And therefore the adding of the ScmManagerApiData action
        //Which is needed for the fetching of custom properties
        project.scheduleBuild2(0);
      }
    });
  }
}
