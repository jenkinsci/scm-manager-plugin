# Jenkins scm-manager-plugin
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Fscm-manager-plugin%2Fmaster)](https://ci.jenkins.io/job/Plugins/job/scm-manager-plugin/job/master/)
[![License](https://img.shields.io/github/license/jenkinsci/github-plugin.svg)](LICENSE)

Jenkins SCM-Manager 2 Plugin

## Description

Once the plugin is installed, it will automatically detect jobs which have configured scm that points to an instance of SCM-Manager v2.
If such a build job is started, the plugin will send the `PENDING` state for the checkout revision to SCM-Manager.
After the build is complete the plugin will send the resulting state to SCM-Manager (`SUCCESS`, `UNSTABLE` or `FAILURE`).

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
