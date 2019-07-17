#!groovy
@Library('github.com/cloudogu/ces-build-lib@6cff6d9d')
import com.cloudogu.ces.cesbuildlib.*

properties([
  // Keep only the most recent builds in order to preserve space
  buildDiscarder(logRotator(numToKeepStr: '20')),
  // Don't run concurrent builds for a branch, because they use the same workspace directory
  disableConcurrentBuilds()
])

node {

  def javaHome = tool 'JDK8'
  Maven mvn = new MavenWrapper(this, javaHome)
  mvn.additionalArgs = "-DperformRelease"
  Git git = new Git(this)

  catchError {

    stage('Checkout') {
      checkout scm
      git.clean('')
    }

    initMaven(mvn)

    stage('Build') {
      mvn 'clean install -DskipTests'
      archive '**/target/*.jar'
    }

    stage('Unit Test') {
      mvn 'test'
    }

    stage('Integration Test') {
      mvn 'verify -DskipUnitTests'
    }

    stage('Static Code Analysis') {
      def sonarQube = new SonarCloud(this, [sonarQubeEnv: 'sonarcloud.io-cloudogu'])

      sonarQube.analyzeWith(mvn)

      if (!sonarQube.waitForQualityGateWebhookToBeCalled()) {
        currentBuild.result ='UNSTABLE'
      }
    }

    stage('Archive') {
      archive 'target/*.hpi'
    }

  }

  // Archive Unit and integration test results, if any
  junit allowEmptyResults: true,
    testResults: '**/target/surefire-reports/TEST-*.xml, **/target/failsafe-reports/*.xml'

  // Find maven warnings and visualize in job
  warnings consoleParsers: [[parserName: 'Maven']]

  mailIfStatusChanged(git.commitAuthorEmail)
}
