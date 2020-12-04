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

import com.sun.corba.ee.spi.transport.connection.ContactInfo;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ContactInfoImpl implements ContactInfo<ConnectionImpl> {
    private String address ;

    private static AtomicLong nextId =
        new AtomicLong() ;
    private static AtomicBoolean simulateAddressUnreachable = 
        new AtomicBoolean() ;

    private static ConcurrentMap<String,ContactInfoImpl> cinfoMap =
        new ConcurrentHashMap<String,ContactInfoImpl>() ;

    private RandomDelay rdel ;

    private ContactInfoImpl( String address, int minDelay, int maxDelay ) {
        this.address = address ;
        rdel = new RandomDelay( minDelay, maxDelay ) ;
    }

    public static ContactInfoImpl get( String address ) {
        return get( address, 0, 0 ) ;
    }

    public static ContactInfoImpl get( String address, int minDelay, int maxDelay ) {
        ContactInfoImpl result = new ContactInfoImpl( address, minDelay, maxDelay ) ;
        ContactInfoImpl entry = cinfoMap.putIfAbsent( address, result ) ;
        if (entry == null)
            return result ;
        else
            return entry ;
    }

    public void remove( String address ) {
        cinfoMap.remove( address ) ;
    }

    public void setUnreachable( boolean arg ) {
        simulateAddressUnreachable.set( arg ) ;
    }

    public ConnectionImpl createConnection() throws IOException {
        if (simulateAddressUnreachable.get()) {
            throw new IOException( "Address " + address 
                + " is currently unreachable" ) ;
        } else {
            long id = nextId.getAndIncrement() ;
            ConnectionImpl result = new ConnectionImpl( address, id, this ) ;
            return result ;
        }
    }

    @Override
    public String toString() {
        return "ContactInfoImpl[" + address + "]" ;
    }
}

