#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
#

. ../cluster.props.ssh
S1AS_HOME="${instance1_s1as_home}/glassfish"
ASADMIN="${S1AS_HOME}/bin/asadmin"
set -x
${ASADMIN} start-domain
${ASADMIN} enable-secure-admin
${ASADMIN}  --user admin create-node-ssh --nodehost ${das_node} --installdir ${instance1_s1as_home} agent1
${ASADMIN} create-cluster ${cluster_name}
${ASADMIN} --host ${das_node} --port ${das_port} create-instance --cluster ${cluster_name} --systemproperties instance_name=in1 --node agent1 in1
 
${ASADMIN} --host ${das_node} --port ${das_port} create-instance --cluster ${cluster_name} --systemproperties instance_name=in2 --node agent1 in2

${ASADMIN} --host ${das_node} --port ${das_port} create-instance --cluster ${cluster_name} --systemproperties instance_name=in3 --node agent1 in3

${ASADMIN}  --host ${das_node} --port ${das_port} start-cluster ${cluster_name}
