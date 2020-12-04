/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
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

