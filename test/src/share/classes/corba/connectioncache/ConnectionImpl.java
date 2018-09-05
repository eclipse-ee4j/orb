/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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

