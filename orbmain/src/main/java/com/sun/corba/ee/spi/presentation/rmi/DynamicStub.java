/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.presentation.rmi ;

import java.rmi.RemoteException ;

import org.omg.CORBA.portable.Delegate ;
import org.omg.CORBA.portable.OutputStream ;

import org.omg.CORBA.ORB ;

/** Interface used to support dynamically generated stubs.
 * This supplies some methods that are found in 
 * org.omg.CORBA.portable.ObjectImpl that are not available
 * in org.omg.CORBA.Object.
 */
public interface DynamicStub extends org.omg.CORBA.Object
{
    /** Similar to ObjectImpl._set_delegate
     * 
     * @param delegate delegate to set
     * @see org.omg.CORBA.portable.ObjectImpl#_set_delegate(org.omg.CORBA.portable.Delegate)
     */
    void setDelegate( Delegate delegate ) ;

    /** Similar to ObjectImpl._get_delegate
     * 
     * @return Delegate contained
     * @see org.omg.CORBA.portable.ObjectImpl#_get_delegate() 
     */
    Delegate getDelegate() ;

    /** Similar to ObjectImpl._orb()
     * 
     * @return the ORB instance that created the Delegat
     * @see org.omg.CORBA.portable.ObjectImpl#_orb()
     */
    ORB getORB() ;

    /** Similar to ObjectImpl._ids
     * 
     * @return the array of all repository identifiers
     * @see org.omg.CORBA.portable.ObjectImpl#_ids()
     */
    String[] getTypeIds() ; 

    /** Connect this dynamic stub to an ORB.
     * Just as in standard RMI-IIOP, this is required after
     * a dynamic stub is deserialized from an ObjectInputStream.
     * It is not needed when unmarshalling from a 
     * org.omg.CORBA.portable.InputStream.
     * 
     * @param orb ORB to connect to
     */
    void connect( ORB orb ) throws RemoteException ;

    boolean isLocal() ;

    OutputStream request( String operation, boolean responseExpected ) ;
}

