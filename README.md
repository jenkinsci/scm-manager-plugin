# SCM-Manager Plugin
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Fscm-manager-plugin%2Fmaster)](https://ci.jenkins.io/job/Plugins/job/scm-manager-plugin/job/master/)
[![License](https://img.shields.io/github/license/jenkinsci/github-plugin.svg)](LICENSE)

Jenkins [SCM-Manager 2](https://www.scm-manager.org/) Plugin

## Description

This plugin gives various ways to connect your SCM-Manager with Jenkins, like sending build status, multibranch
pipelines, folders for complete namespaces and navigation items.

### Build Status
Once the plugin is installed, it will automatically detect jobs which have configured scm that points to an instance
of SCM-Manager v2. If such a build job starts, the plugin will send the `PENDING` state for the checkout revision to
SCM-Manager. After the build is complete the plugin will send the resulting state to SCM-Manager (`SUCCESS`, `UNSTABLE`
or `FAILURE`).

### Multibranch Pipelines
For multibranch pipelines there is a new branch source named "SCM-Manager", so you can create pipelines in Jenkins that
can find branches, tags and pull requests in SCM-Manager hosted repositories on their own. In cooperation with the
[jenkins plugin](https://www.scm-manager.org/plugins/scm-jenkins-plugin/) in SCM-Manager the Jenkins ci server will receive hooks on
every change and trigger new builds.
To remove stale jobs like for no longer available repositories, you can manually execute "Scan Namespace Now".

To create such a pipeline, select "New Item" on the Jenkins top level page, then enter a name and select "Multibranch
Pipeline".

![](docs/de/assets/select-multibranch-pipeline.png)

In the configuration form for this item, select the matching "SCM-Manager" entry for your repository type in the "Add
source" dropdown in the section "Branch Sources".

![](docs/de/assets/config-multibranch-pipeline-source.png)

In the new configuration enter the base URL of your SCM-Manager instance and select according credentials. This
plugin will then load a list of all available repositories you can select from.

![](docs/de/assets/config-multibranch-pipeline.png)

Finally you can select behaviours where you can choose for example, whether branches, tags or pull requests shall be
build.

#### JobDSL

In order to create a SCM-Manager Mercurial or Git repository with the Jenkins JobDSL the following syntax can be used:

```groovy
multibranchPipelineJob('heart-of-gold') {
  branchSources {
    scmManager {
      id('spaceships/heart-of-gold')
      serverUrl('https://scm.hitchhiker.com')
      credentialsId('my-secret-id')
      repository('spaceships/heart-of-gold')
      discoverBranches(true)
      discoverPullRequest(true)
      discoverTags(false)
    }
  }
}
```

The parameters `discoverBranches`, `discoverPullRequest` und `discoverTags` are optional 
and describe which heads of the repository are build.
The example shows the default values.

For a Subversion repository have a look at the following example:

```groovy
multibranchPipelineJob('heart-of-gold') {
  branchSources {
    scmManagerSvn {
      id('spaceships/heart-of-gold')
      serverUrl('https://scm.hitchhiker.com')
      credentialsId('my-secret-id')
      repository('spaceships/heart-of-gold')
      includes("trunk,branches/*,tags/*,sandbox/*")
      excludes("")
    }
  }
}
```
The parameters for `includes` und `excludes` are optional and describe which directories of the repository are build.
The example shows the default values.

### Namespaces
If you want to have build jobs for every repository in a namespace, you can create "SCM-Manager namespace" jobs. These
will scan all repositories in the given namespace and create multibranch pipelines for each repository where a
`Jenkinsfile` can be found. If a new repository is created in this namespace, a new pipeline will be created
automatically.

To create such a folder for a complete namespace, select "New Item" on the Jenkins top level page, then enter a name
and select "SCM-Manager Namespace".

![](docs/de/assets/select-namespace-item.png)

In the configuration form for this item, enter the base URL of your SCM-Manager instance and select according
credentials. This plugin will then load a list of all available namespaces you can select from.

As an alternative to a concrete namespace you can select <code>--all--</code> if you want to create build jobs for
all repositories of the whole SCM-Manager instance. Please note, that in this case the folders for the jobs will be
named with the pattern *namespace/name*.

![](docs/de/assets/config-namespace-item.png)

Finally you can select behaviours where you can choose for example, whether branches, tags or pull requests shall be
build.

#### JobDSL

In order to create a build job for an SCM-Manager namespace with the job dsl, have a look at the following example:

```groovy
organizationFolder("spaceships") {
  organizations {
    scmManagerNamespace {
      serverUrl('https://scm.hitchhiker.com')
      credentialsId('my-secret-id')
      namespace("spaceships")
      discoverBranches(true)
      discoverPullRequest(true)
      discoverTags(false)
      discoverSvn {
        includes("trunk,branches/*,tags/*,sandbox/*")
        excludes("")
      }
    }
  }
}
// scan namespace directly after creation
queue("spaceships")
```

The `discover*` parameters are optional, the example above shows the default values. 
To disable subversion builds, a `false` can be passed to the `discoverSvn` method e.g.: `discoverSvn(false)`.
To build all namespaces of the SCM-Manager instance, the pseudo namespace `--all--` can be used.

### Navigation
In different pages of Jenkins you can find links to the corresponding SCM-Manager page:

- In multibranch pipelines you can find the link "SCM-Manager" in the main navigation on the left. This leads you
  directly to the main page of the repository in SCM-Manager.
- In a job for a concrete branch or a tag the link "SCM-Manager" in the main navigation on the left will get you
  directly to the sources in SCM-Manager for this branch or tag.
- In a job for a pull request the link "SCM-Manager" in the main navigation on the left will navigate to the pull
  request in SCM-Manager.
- From the page for a build result the link "SCM-Manager" in the main navigation on the left leads to the sources
  view in SCM-Manager for the concrete revision that has been build.
- In the "Changes" view there are links that lead to the details of a change showing the authors, the commit message
  and the diff for the commit.

## Requirements

For full functionality, this plugin requires SCM-Manager v2 with an installed
[jenkins plugin](https://www.scm-manager.org/plugins/scm-jenkins-plugin/) for the triggers and the
[CI plugin](https://www.scm-manager.org/plugins/scm-ci-plugin/) to show the build results inside SCM-Manager. To create
ssh connection between Jenkins and SCM-Manager you can use the
[SSH plugin](https://www.scm-manager.org/plugins/scm-ssh-plugin/).

## Build

To build the plugin just run the maven package phase.

```bash
mvn package
```

## Development 

Start the local Jenkins instance:

```bash
mvn hpi:run
```
