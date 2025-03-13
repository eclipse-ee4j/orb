/*
 *  Copyright (c) 2020, 2025 Contributors to the Eclipse Foundation..
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
        sh 'mvn -Pstaging,all-tests,dash-licenses clean install'
        junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
        archiveArtifacts artifacts: 'dash-summary.txt'
      }
    }
  }
}
