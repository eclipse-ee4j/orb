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

package com.sun.corba.ee.spi.folb;

import java.io.Serializable ;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

/** Class representing endpoint info for the ORB.
 *
 * @author ken
 */
public class SocketInfo implements Serializable {
    private final String type ;
    private final String host ;
    private final int port ;

    public SocketInfo( InputStream is ) {
        this.type = is.read_string() ;
        this.host = is.read_string() ;
        this.port = is.read_long() ;
    }

    public SocketInfo( String type, String host, int port ) {
        this.type = type ;
        this.host = host ;
        this.port = port ;
    }

    public String type() { return type ; }
    public String host() { return host ; }
    public int port() { return port ; }

    public void write( OutputStream os ) {
        os.write_string( type ) ;
        os.write_string( host ) ;
        os.write_long(port);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( "SocketInfo[" ) ;
        sb.append( "type=" ) ;
        sb.append( type ) ;
        sb.append( " host=" ) ;
        sb.append( host ) ;
        sb.append( " port=" ) ;
        sb.append( port ) ;
        sb.append( ']' ) ;
        return sb.toString() ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final SocketInfo other = (SocketInfo) obj;

        if ((this.type == null) ? (other.type() != null)
            : !this.type.equals(other.type())) {

            return false;
        }

        if ((this.host == null) ? (other.host() != null)
            : !this.host.equals(other.host())) {

            return false;
        }

        if (this.port != other.port()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 71 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 71 * hash + this.port;
        return hash;
    }
}
