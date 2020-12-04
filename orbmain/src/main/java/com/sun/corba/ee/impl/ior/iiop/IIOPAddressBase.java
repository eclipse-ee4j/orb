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

package com.sun.corba.ee.impl.ior.iiop ;

import org.omg.CORBA.BAD_PARAM ;

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;

abstract class IIOPAddressBase implements IIOPAddress
{
    // Ports are marshalled as shorts on the wire.  The IDL
    // type is unsigned short, which lacks a convenient representation
    // in Java in the 32768-65536 range.  So, we treat ports as
    // ints throught this code, except that marshalling requires a
    // scaling conversion.  intToShort and shortToInt are provided
    // for this purpose.
    protected short intToShort( int value ) 
    {
        if (value > 32767)
            return (short)(value - 65536) ;
        return (short)value ;
    }

    protected int shortToInt( short value )
    {
        if (value < 0)
            return value + 65536 ;
        return value ;
    }

    public void write( OutputStream os )
    {
        os.write_string( getHost() ) ;
        int port = getPort() ;
        os.write_short( intToShort( port ) ) ;
    }

    public boolean equals( Object obj )
    {
        if (!(obj instanceof IIOPAddress))
            return false ;

        IIOPAddress other = (IIOPAddress)obj ;

        return getHost().equals(other.getHost()) && 
            (getPort() == other.getPort()) ;
    }

    public int hashCode()
    {
        return getHost().hashCode() ^ getPort() ;
    }

    public String toString()
    {
        return "IIOPAddress[" + getHost() + "," + getPort() + "]" ;
    }
}
