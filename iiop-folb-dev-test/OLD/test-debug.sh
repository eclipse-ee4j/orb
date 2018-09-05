#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
#

set -x
. ../cluster.props
APS_HOME="test/OrbFailOver/build"
S1AS_HOME="${instance1_s1as_home}/glassfish"
ASADMIN="${S1AS_HOME}/bin/asadmin"
APPCLIENT="${S1AS_HOME}/bin/appclient"

# HOST_PORTS=${instance1_node}:${instance1_IIOP_LISTENER_PORT},${instance2_node}:${instance2_IIOP_LISTENER_PORT},${instance3_node}:${instance3_IIOP_LISTENER_PORT} 
HOST_PORTS=${instance1_node}:${instance1_IIOP_LISTENER_PORT},${instance2_node}:${instance2_IIOP_LISTENER_PORT}

######### customized cluster creation 
# ${ASADMIN} start-domain --debug
# ${ASADMIN} create-cluster ${cluster_name}  
# ${ASADMIN} create-local-instance --cluster ${cluster_name} ${instance1_name}  
# ${ASADMIN} create-local-instance --cluster ${cluster_name} ${instance2_name}
######### end customized cluster creation

# ${ASADMIN} start-local-instance ${instance1_name} ; ${ASADMIN} start-local-instance ${instance2_name}

${ASADMIN} deploy --target ${cluster_name} --force ${APS_HOME}/OrbFailOver-ejb.jar

${APPCLIENT} -agentlib:jdwp=transport=dt_socket,address=8118,server=y,suspend=y -Djavax.enterprise.resource.corba.level=WARN -Djava.util.logging.level=WARN -Dcom.sun.appserv.iiop.endpoints=${HOST_PORTS} -client  ${APS_HOME}/OrbFailOver-app-client.jar -targetserver ${HOST_PORTS} -name OrbFailOver-app-client  $@

# sleep 30
# ${ASADMIN} stop-instance ${instance1_name}

