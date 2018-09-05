#!/bin/bash -x
#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
#

# sample local test execution script
source ./setup.sh
############################
glassfish4/glassfish/bin/asadmin start-domain
glassfish4/glassfish/bin/asadmin  create-jvm-options -Djava.rmi.server.useCodebaseOnly=true
glassfish4/glassfish/bin/asadmin stop-domain
pause
############################
#./run.sh 
#./run.sh -include "lbfail"
./run.sh -exclude "15804sfsb,15804sfsb_kill,15804sfsb_kill_delete"

