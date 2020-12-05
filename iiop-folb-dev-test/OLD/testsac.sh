#! /bin/sh
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

. ../cluster.props
APS_HOME="test/OrbFailOver/build"
S1AS_HOME="${instance1_s1as_home}/glassfish"
ASADMIN="${S1AS_HOME}/bin/asadmin"
SACLIENT="java -Drmiregistry.host=minas -classpath ${S1AS_HOME}/modules/gf-client.jar:${APS_HOME}/OrbFailOver-app-client.jar:${APS_HOME}/OrbFailOver-ejb.jar"

PROPS_FILE=${PWD}/logging.properties
DEBUG_ARGS="-agentlib:jdwp=transport=dt_socket,address=8118,server=y,suspend=y"

# HOST_PORTS=${instance1_node}:${instance1_IIOP_LISTENER_PORT},${instance2_node}:${instance2_IIOP_LISTENER_PORT},${instance3_node}:${instance3_IIOP_LISTENER_PORT} 
HOST_PORTS=${instance1_node}:${instance1_IIOP_LISTENER_PORT},${instance2_node}:${instance2_IIOP_LISTENER_PORT}

set -x
${ASADMIN} deploy --target ${cluster_name} --force ${APS_HOME}/OrbFailOver-ejb.jar

#${SACLIENT} -Djavax.enterprise.resource.corba.level=FINE -Djava.util.loggin.level=FINE -client  ${APS_HOME}/OrbFailOver-app-client.jar -targetserver ${instance1_node}:${instance1_IIOP_LISTENER_PORT},${instance2_node}:${instance2_IIOP_LISTENER_PORT} -name OrbFailOver-app-client &

if [ "${DEBUGGER}" = "1" ];
then ${SACLIENT} ${DEBUG_ARGS} -Djava.util.logging.config.file=${PROPS_FILE} -Dcom.sun.appserv.iiop.endpoints=${HOST_PORTS} -Dtest.folb.asadmin.command=${ASADMIN} orbfailover.Main $@ ;
else
${SACLIENT} -Djava.util.logging.config.file=${PROPS_FILE} -Dcom.sun.appserv.iiop.endpoints=${HOST_PORTS} -Dtest.folb.asadmin.command=${ASADMIN} orbfailover.Main $@ ;
fi
