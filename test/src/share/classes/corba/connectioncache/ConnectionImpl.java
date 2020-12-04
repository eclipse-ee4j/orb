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

package corba.connectioncache ;
    
// A simple implementation of Connection for testing.  No 

import com.sun.corba.ee.spi.transport.connection.Connection;
import java.util.concurrent.atomic.AtomicBoolean;

// synchronization is required to use this class.  
public class ConnectionImpl implements Connection {
    private String name ;
    private long id ;
    private ContactInfoImpl cinfo ;
    private AtomicBoolean isClosed ;

    public ConnectionImpl( String name, long id, ContactInfoImpl cinfo ) {
        this.name = name ;
        this.id = id ;
        this.cinfo = cinfo ;
        this.isClosed = new AtomicBoolean() ;
    }

    public ContactInfoImpl getContactInfo() {
        return cinfo ;
    }

    // Simulate access (read/write) to a connection to make sure
    // we do not access a closed connection.
    public void access() {
        if (isClosed.get())
            throw new RuntimeException( "Illegal access: connection " 
                + name + " is closed." ) ;
    }

    public void close() {
        boolean wasClosed = isClosed.getAndSet( true ) ;
        if (wasClosed)
            throw new RuntimeException( 
                "Attempting to close connection " ) ;
    }

    @Override
    public String toString() {
        return "ConnectionImpl[" + name + ":" + id + "]" ;
    }
}

