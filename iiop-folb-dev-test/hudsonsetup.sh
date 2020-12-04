#
# Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
# v. 1.0 which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# This Source Code may also be made available under the following Secondary
# Licenses when the conditions for such availability set forth in the Eclipse
# Public License v. 2.0 are satisfied: GNU General Public License v2.0
# w/Classpath exception which is available at
# https://www.gnu.org/software/classpath/license.html.
#
# SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
# Classpath-exception-2.0
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

