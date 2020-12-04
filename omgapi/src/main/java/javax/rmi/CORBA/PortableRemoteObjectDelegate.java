/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package javax.rmi.CORBA;        

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;

/**
 * Supports delegation for method implementations in {@link javax.rmi.PortableRemoteObject}.
 * The delegate is a singleton instance of a class that implements this
 * interface and provides a replacement implementation for all the
 * methods of <code>javax.rmi.PortableRemoteObject</code>.
 *
 * Delegates are enabled by providing the delegate's class name as the
 * value of the 
 * <code>javax.rmi.CORBA.PortableRemoteObjectClass</code>
 * system property.
 *
 * @see javax.rmi.PortableRemoteObject
 */
public interface PortableRemoteObjectDelegate {

    /**
     * Delegation call for {@link javax.rmi.PortableRemoteObject#exportObject}.
     * @param obj object to export
     * @throws RemoteException if the object cannot be exported
     */
    void exportObject(Remote obj)
        throws RemoteException;

    /**
     * Delegation call for {@link javax.rmi.PortableRemoteObject#toStub}.
     * @param obj remote to convert to stub
     * @return stub of the remote
     * @throws NoSuchObjectException if the object does not exist
     */
    Remote toStub (Remote obj)   
        throws NoSuchObjectException;

    /**
     * Delegation call for {@link javax.rmi.PortableRemoteObject#unexportObject}.
     * @param obj object to unremove
     * @throws NoSuchObjectException if the object does not exist
     */
    void unexportObject(Remote obj) 
        throws NoSuchObjectException;

    /**
     * Delegation call for {@link javax.rmi.PortableRemoteObject#narrow}.
     * @param narrowFrom object to narrow from
     * @param narrowTo target to narrow to
     * @return object of the desired type
     * @throws ClassCastException if the object cannot be narrowed 
     */
    java.lang.Object narrow (java.lang.Object narrowFrom,
                                    java.lang.Class narrowTo)
        throws ClassCastException;

    /**
     * Delegation call for {@link javax.rmi.PortableRemoteObject#connect}.
     * @param target remote object to connect
     * @param source starting object
     * @throws RemoteException if an error occurred connecting
     */
    void connect (Remote target, Remote source)
        throws RemoteException;

}                                            
