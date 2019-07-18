# Jenkins scm-manager-plugin
[![Build Status](https://oss.cloudogu.com/jenkins/buildStatus/icon?job=cloudogu-github/jenkins-scm-manager-plugin/master)](https://oss.cloudogu.com/jenkins/blue/organizations/jenkins/cloudogu-github%2Fjenkins-scm-manager-plugin/branches/)
[![Quality Gates](https://sonarcloud.io/api/project_badges/measure?project=io.jenkins.plugins%3Ascm-manager&metric=alert_status)](https://sonarcloud.io/dashboard?id=io.jenkins.plugins%3Ascm-manager)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=io.jenkins.plugins%3Ascm-manager&metric=coverage)](https://sonarcloud.io/dashboard?id=io.jenkins.plugins%3Ascm-manager)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=io.jenkins.plugins%3Ascm-manager&metric=sqale_index)](https://sonarcloud.io/dashboard?id=io.jenkins.plugins%3Ascm-manager)
[![License](https://img.shields.io/github/license/jenkinsci/github-plugin.svg)](LICENSE)

Jenkins plugin for the upcoming version 2 of SCM-Manager.

## Description

Once the plugin is installed, it will automatically detect jobs which have configured scm that points to an instance of SCM-Manager v2.
If such a build job is started, the plugin will send the `PENDING` state for the checkout revision to SCM-Manager.
After the build is complete the plugin will send the resulting state to SCM-Manager (`SUCCESS` or `FAILURE`).

## Requirements

The plugin requires SCM-Manager v2 with an installed `scm-ci-plugin`.

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
