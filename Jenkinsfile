/*
 *  Copyright (c) 2020, 2025 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
env.label = "orb-ci-pod-${UUID.randomUUID().toString()}"
pipeline {
  options {
    // keep at most 50 builds
    buildDiscarder(logRotator(numToKeepStr: '50'))
    // abort pipeline if previous stage is unstable
    skipStagesAfterUnstable()
    // show timestamps in logs
    timestamps()
    // timeout, abort after 60 minutes
    timeout(time: 60, unit: 'MINUTES')
  }
  agent any
  tools {
    maven 'apache-maven-latest'
    jdk 'temurin-jdk21-latest'
  }
  stages {
    stage('build') {
      steps {
        sh 'mvn -Psnapshots,all-tests,dash-licenses clean install'
      }
    }
  }
  post {
    always {
      // Reporting and diagnostics live in post, not in the stage above. A failing mvn aborts the
      // stage, so any step written after the sh is skipped - which meant a failed build published
      // neither test results nor artifacts, precisely when they are most needed.

      // The functional suite reports through Surefire like every other module, so this glob picks
      // it up too. allowEmptyResults stays false deliberately: finding no reports means the suite
      // did not run, which is a failure, not a pass.
      junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: false

      // Per-process output from the functional harness. It forks ORBD/server/client JVMs and
      // writes each one's stdout and stderr under functional-tests/target/gen/<package>/ as
      // client.out.txt, client.err.txt, server.err.txt, ORBD.out.txt and so on. When an entry
      // fails, the console shows only "Bad exit value(s): client[1]" - the reason is in these
      // files, and without them a CI-only failure cannot be diagnosed off the console log.
      // Small: a few hundred KB for a full run.
      archiveArtifacts artifacts: 'functional-tests/target/gen/**/*.txt', allowEmptyArchive: true

      // Written by the dash-licenses profile; legitimately absent if the build failed earlier,
      // hence allowEmptyArchive here rather than failing the build a second time over it.
      archiveArtifacts artifacts: 'dash-summary.txt', allowEmptyArchive: true
    }
  }
}
