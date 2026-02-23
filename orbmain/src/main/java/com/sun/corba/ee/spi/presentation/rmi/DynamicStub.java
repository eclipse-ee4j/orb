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

package com.sun.corba.ee.spi.presentation.rmi ;

import java.rmi.RemoteException ;

import org.omg.CORBA.ORB ;
import org.omg.CORBA.portable.Delegate ;
import org.omg.CORBA.portable.OutputStream ;

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
     * @throws RemoteException if unable to connect
     */
    void connect( ORB orb ) throws RemoteException ;

    boolean isLocal() ;

    OutputStream request( String operation, boolean responseExpected ) ;
}

