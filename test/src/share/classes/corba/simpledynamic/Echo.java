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

package corba.simpledynamic;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

import corba.misc.BuckPasserAL  ;
import corba.misc.BuckPasserV  ;

public interface Echo extends Remote {
    String sayHello( Object obj ) throws RemoteException ;

    Echo say( Echo obj ) throws RemoteException ;

    String name() throws RemoteException ;

    Object testExceptionContext() throws RemoteException ;
    /** Can send enough data to force fragmentation
     */
    int[] echo( int[] arg ) throws RemoteException ;

    Object echo( Object obj ) throws RemoteException ;

    BuckPasserAL echo( BuckPasserAL arg ) throws RemoteException ;
    BuckPasserV echo( BuckPasserV arg ) throws RemoteException ;
    BuckPasserVectorOriginal echo( BuckPasserVectorOriginal arg ) throws RemoteException ;
}


