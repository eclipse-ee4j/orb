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

package com.sun.corba.ee.impl.naming.namingutil;

import com.sun.corba.ee.spi.misc.ORBConstants;

/** 
 *  EndpointInfo is used internally by CorbaLoc object to store the
 *  host information used in creating the Service Object reference
 *  from the -ORBInitDef and -ORBDefaultInitDef definitions.
 *
 *  @author Hemanth
 */
public class IIOPEndpointInfo
{
    // Version information
    private int major, minor;

    // Host Name and Port Number
    private String host;
    private int port;

    public String toString() {
        return "IIOPEndpointInfo[" + major + "." + minor
            + " " + host + ":" + port + "]" ;
    }

    IIOPEndpointInfo( ) {
        // Default IIOP Version 
        major = ORBConstants.DEFAULT_INS_GIOP_MAJOR_VERSION;
        minor = ORBConstants.DEFAULT_INS_GIOP_MINOR_VERSION;
        // Default host is localhost
        host = ORBConstants.DEFAULT_INS_HOST;
        // Default INS Port
        port = ORBConstants.DEFAULT_INS_PORT;
    }

    public void setHost( String theHost ) {
        host = theHost;
    }

    public String getHost( ) {
        return host;
    }

    public void setPort( int thePort ) {
        port = thePort;
    }

    public int getPort( ) {
        return port;
    }

    public void setVersion( int theMajor, int theMinor ) {
        major = theMajor;
        minor = theMinor;
    }

    public int getMajor( ) {
        return major;
    }

    public int getMinor( ) {
        return minor;
    }

    /** Internal Debug Method.
     */
    public void dump( ) {
        System.out.println( " Major -> " + major + " Minor -> " + minor );
        System.out.println( "host -> " + host );
        System.out.println( "port -> " + port );
    }
}

