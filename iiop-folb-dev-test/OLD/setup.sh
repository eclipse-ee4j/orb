#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
#

. ../cluster.props
S1AS_HOME="${instance1_s1as_home}/glassfish"
ASADMIN="${S1AS_HOME}/bin/asadmin"
set -x
PROPS_FILE=${PWD}/logging.properties
${ASADMIN} create-system-properties --target ${cluster_name} "java.util.logging.config.file=${PROPS_FILE}"
${ASADMIN} create-system-properties java.util.logging.config.file=${PROPS_FILE}

