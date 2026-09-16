# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

<!-- Historical entries use the publication dates and notes from
https://github.com/jenkinsci/scm-manager-plugin/releases. -->

## Unreleased

### Fixed

- Handling of SVN repositories in organization folders ([JENKINS-76041](https://issues.jenkins.io/browse/JENKINS-76041), [#80](https://github.com/jenkinsci/scm-manager-plugin/pull/80))

## 1.12.1 - 2026-08-14

### Added

- Option to skip pull request builds for drafts ([#79](https://github.com/jenkinsci/scm-manager-plugin/pull/79))

### Fixed

- Missing permission checks that allowed SSRF and credentials capture ([#81](https://github.com/jenkinsci/scm-manager-plugin/pull/81))

## 1.11.1 - 2025-11-18

### Changed

- Special characters in custom property keys are now replaced with underscores in environment variables

## 1.11.0 - 2025-11-13

### Added

- Integration of custom properties for multibranch pipelines with SCM-Manager source

## 1.10.0 - 2025-02-10

### Changed

- Replaced repository select with a combobox
- Updated to Jenkins 2.479.2 / Java 17

## 1.9.4 - 2024-05-13

### Fixed

- Close responses to avoid leaked connections ([#60](https://github.com/jenkinsci/scm-manager-plugin/pull/60))

## 1.9.3 - 2024-04-18

### Fixed

- Close response bodies correctly ([#59](https://github.com/jenkinsci/scm-manager-plugin/pull/59))
- Tests updated for Java 17 ([#58](https://github.com/jenkinsci/scm-manager-plugin/pull/58))

## 1.9.2 - 2024-03-04

### Fixed

- Handle missing branches in pull requests ([#56](https://github.com/jenkinsci/scm-manager-plugin/pull/56))
- Disable automatic redirects to detect the CAS login page ([#57](https://github.com/jenkinsci/scm-manager-plugin/pull/57))

## 1.9.1 - 2023-06-30

### Fixed

- Short timeout for SCM-Manager requests increased to 60 seconds ([#55](https://github.com/jenkinsci/scm-manager-plugin/pull/55))

## 1.9.0 - 2023-06-20

### Changed

- Migrated to the OkHttp library for Java 17 support ([#54](https://github.com/jenkinsci/scm-manager-plugin/pull/54))
- Set the minimum required Jenkins version to 2.387.1 ([#54](https://github.com/jenkinsci/scm-manager-plugin/pull/54))

## 1.8.0 - 2023-05-19

### Added

- Send replaced branch for pull request builds ([#53](https://github.com/jenkinsci/scm-manager-plugin/pull/53))

## 1.7.11 - 2023-05-05

### Fixed

- Introduce a new dedicated SCMM branch discovery trait ([#52](https://github.com/jenkinsci/scm-manager-plugin/pull/52))

## 1.7.10 - 2023-05-02

### Fixed

- Fix the "exclude branch build" flag ([#51](https://github.com/jenkinsci/scm-manager-plugin/pull/51))

## 1.7.9 - 2022-08-24

### Fixed

- Prevent Jenkins from removing repositories ([#39](https://github.com/jenkinsci/scm-manager-plugin/pull/39))

## 1.7.8 - 2022-02-17

### Fixed

- Make URI comparison more resilient ([#29](https://github.com/jenkinsci/scm-manager-plugin/pull/29))

## 1.7.7 - 2022-02-16

### Changed

- Improve logging of the job information resolver ([#28](https://github.com/jenkinsci/scm-manager-plugin/pull/28))

## 1.7.6 - 2022-02-03

### Fixed

- Send CI status updates to the correct URL when build libraries are used

## 1.7.5 - 2022-02-03

### Fixed

- Build triggers for new branches and pull requests

## 1.7.4 - 2021-04-23

### Fixed

- Deserialization of embedded values in repository objects

## 1.7.3 - 2021-04-07

### Fixed

- Fix class-not-found exception without the Subversion plugin ([#24](https://github.com/jenkinsci/scm-manager-plugin/pull/24))

## 1.7.2 - 2021-03-10

### Changed

- Display a more relevant error if an API request returns invalid JSON

## 1.7.1 - 2021-01-25

### Fixed

- "Exclude Branches with open PRs" option

## 1.7.0 - 2021-01-19

### Added

- Pseudo namespace `--all--` to create builds for all repositories of all namespaces with one navigator
- JobDSL support for navigators

## 1.6.0 - 2020-12-03

### Added

- Option in pull request behaviour to remove the corresponding branch build
- JobDSL support for `ScmManagerSource` and `ScmManagerSvnSource`

## 1.5.1 - 2020-11-18

### Fixed

- Links to branches in SCM-Manager
- Links to SCM-Manager with SSH URLs
- Angry Jenkins error when changing from an SSH URL with SSH credentials to an HTTP URL

## 1.5.0 - 2020-11-14

### Added

- Top-level item to create a navigator for a complete SCM-Manager namespace

## 1.4.0 - 2020-11-04

### Added

- Support for SSH URLs in multibranch pipelines

### Fixed

- Environment variable `CHANGE_BRANCH` for pull request builds

## 1.3.3 - 2020-11-04

### Fixed

- Build of Git repositories on servers without a configured Git user name and email

## 1.3.2 - 2020-11-04

### Fixed

- Apply selected traits correctly

## 1.3.1 - 2020-11-04

### Fixed

- Accept HTTP redirect status 302 after a test request in configuration

## 1.3.0 - 2020-09-09

### Added

- SCM-Manager source support for Mercurial

### Fixed

- Missing URL encoding of branch and tag names

## 1.2.1 - 2020-09-02

### Fixed

- Use the date from the tag if available
- Dequote Git paths for the SCM-Manager repository browser

## 1.2.0 - 2020-08-24

### Added

- SCM-Manager source logo
- Actions to navigate from Jenkins to SCM-Manager
- Repository browser for SCM-Manager

## 1.1.0 - 2020-08-24

### Added

- SCM-Manager source for multibranch pipelines, supporting branches, tags, and pull requests for Git repositories

## 1.0.0 - 2020-08-24

### Added

- Initial release with support for build notifications
