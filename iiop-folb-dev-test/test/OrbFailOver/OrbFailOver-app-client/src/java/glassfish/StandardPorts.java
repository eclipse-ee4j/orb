/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package glassfish;

/** The standard ports GlassFish uses.  The create-instance command returns
 * these in the form (NAME)=nnnn.
 *
 * @author ken_admin
 */
public enum StandardPorts {
    HTTP_LISTENER_PORT,
    HTTP_SSL_LISTENER_PORT,
    IIOP_LISTENER_PORT,
    IIOP_SSL_LISTENER_PORT,
    IIOP_SSL_MUTUALAUTH_PORT,
    JAVA_DEBUGGER_PORT,
    JMX_SYSTEM_CONNECTOR_PORT,
    JMS_PROVIDER_PORT,
    ASADMIN_LISTENER_PORT,
    GMS_LISTENER_PORT,
    OSGI_SHELL_TELNET_PORT ;
} ;

