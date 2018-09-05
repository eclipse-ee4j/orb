#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
#

echo "Start Date: `date`"
GFV3_WS=${HOME}/data/workspace/corba-build-glassfish-trunk/main/appserver
CORBA_WS=${HOME}/data/workspace/corba-staging-build-test-orb
GFV3_WORK=${WORKSPACE}
S1AS_HOME=${GFV3_WORK}/glassfish4/glassfish
export GFV3_WS
export CORBA_WS
export GFV3_WORK
export S1AS_HOME
##########
CORBA_DEVTEST_WS=$WORKSPACE
$CORBA_DEVTEST_WS/scripts/installgfv3
$CORBA_DEVTEST_WS/scripts/installorb
cd $CORBA_DEVTEST_WS/test/OrbFailOver
OPT1="-Dlibs.CopyLibs.classpath=$HOME/bin/org-netbeans-modules-java-j2seproject-copylibstask.jar"
OPT2="-Dj2ee.server.home=${WORKSPACE}/glassfish4"
JAVA_HOME=/usr/jdk/latest
export JAVA_HOME
ant $OPT1 $OPT2 clean
ant $OPT1 $OPT2 
cd $CORBA_DEVTEST_WS
export DEBUGGER=0

