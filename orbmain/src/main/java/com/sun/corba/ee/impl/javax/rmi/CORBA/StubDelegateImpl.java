/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package com.sun.corba.ee.impl.javax.rmi.CORBA;

import java.io.IOException;

import java.rmi.RemoteException;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.impl.ior.StubIORImpl ;
import com.sun.corba.ee.impl.presentation.rmi.StubConnectImpl ;

import com.sun.corba.ee.spi.logging.UtilSystemException ;

/**
 * Base class from which all static RMI-IIOP stubs must inherit.
 */
public class StubDelegateImpl implements javax.rmi.CORBA.StubDelegate 
{
    private static final UtilSystemException wrapper =
        UtilSystemException.self ;

    private StubIORImpl ior ;

    public synchronized StubIORImpl getIOR() 
    {
        return ior ;
    }
    
    public synchronized void setIOR( StubIORImpl ior ) 
    {
        this.ior = ior ;
    }

    public StubDelegateImpl() 
    {
        ior = null ;
    }

    /**
     * Sets the IOR components if not already set.
     */
    private synchronized void init (javax.rmi.CORBA.Stub self) 
    {
        // If the Stub is not connected to an ORB, BAD_OPERATION exception
        // will be raised by the code below.
        if (ior == null) {
            ior = new StubIORImpl(self);
        }
    }
        
    /**
     * Returns a hash code value for the object which is the same for all stubs
     * that represent the same remote object.
     * @return the hash code value.
     */
    public synchronized int hashCode(javax.rmi.CORBA.Stub self) 
    {
        init(self);
        return ior.hashCode() ;
    }

    /**
     * Compares two stubs for equality. Returns <code>true</code> when used to compare stubs
     * that represent the same remote object, and <code>false</code> otherwise.
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the <code>obj</code>
     *          argument; <code>false</code> otherwise.
     */
    public synchronized boolean equals(javax.rmi.CORBA.Stub self, java.lang.Object obj) 
    {
        if (self == obj) {
            return true;    
        }
        
        if (!(obj instanceof javax.rmi.CORBA.Stub)) {
            return false;            
        }
        
        // no need to call init() because of calls to hashCode() below

        javax.rmi.CORBA.Stub other = (javax.rmi.CORBA.Stub) obj;
        if (other.hashCode() != self.hashCode()) {
            return false;
        }

        // hashCodes being the same does not mean equality. The stubs still
        // could be pointing to different IORs. So, do a literal comparison.
        // Apparently the ONLY way to do this (other than using private 
        // reflection) is toString, because it is not possible to directly
        // access the StubDelegateImpl from the Stub.
        return self.toString().equals( other.toString() ) ;
    }

    @Override
    public synchronized boolean equals( Object obj )
    {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof StubDelegateImpl)) {
            return false;
        }

        StubDelegateImpl other = (StubDelegateImpl)obj ;

        if (ior == null) {
            return ior == other.ior;
        } else {
            return ior.equals(other.ior);
        }
    }

    @Override
    public synchronized int hashCode() {
        if (ior == null) {
            return 0;
        } else {
            return ior.hashCode();
        }
    }

    /**
     * Returns a string representation of this stub. Returns the same string
     * for all stubs that represent the same remote object.
     * @return a string representation of this stub.
     */
    public synchronized String toString(javax.rmi.CORBA.Stub self) 
    {
        if (ior == null) {
            return null;
        } else {
            return ior.toString();
        }
    }
    
    /**
     * Connects this stub to an ORB. Required after the stub is deserialized
     * but not after it is demarshalled by an ORB stream. If an unconnected
     * stub is passed to an ORB stream for marshalling, it is implicitly 
     * connected to that ORB. Application code should not call this method
     * directly, but should call the portable wrapper method 
     * {@link javax.rmi.PortableRemoteObject#connect}.
     * @param orb the ORB to connect to.
     * @exception RemoteException if the stub is already connected to a different
     * ORB, or if the stub does not represent an exported remote or local object.
     */
    public synchronized void connect(javax.rmi.CORBA.Stub self, ORB orb) 
        throws RemoteException 
    {
        ior = StubConnectImpl.connect( ior, self, self, orb ) ;
    }

    /**
     * Serialization method to restore the IOR state.
     */
    public synchronized void readObject(javax.rmi.CORBA.Stub self, 
        java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException 
    {
        if (ior == null) {
            ior = new StubIORImpl();
        }

        ior.doRead( stream ) ;
    }

    /**
     * Serialization method to save the IOR state.
     * @serialData The length of the IOR type ID (int), followed by the IOR type ID
     * (byte array encoded using ISO8859-1), followed by the number of IOR profiles
     * (int), followed by the IOR profiles.  Each IOR profile is written as a 
     * profile tag (int), followed by the length of the profile data (int), followed
     * by the profile data (byte array).
     */
    public synchronized void writeObject(javax.rmi.CORBA.Stub self, 
        java.io.ObjectOutputStream stream) throws IOException 
    {
        init(self);
        ior.doWrite( stream ) ;
    }
}
