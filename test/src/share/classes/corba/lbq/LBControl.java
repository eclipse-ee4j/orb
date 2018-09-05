/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.lbq ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

/** Interface for a remote object used to control the software
 * load balancer.
 */
public interface LBControl extends Remote {
    /** After accepting numConnections, add port to the pool.
     * Each call to add or remove is added to a queue of commands.
     */
    void add( int numConnections, int port ) throws RemoteException ;

    /** After accepting numConnections, remove port from the pool.
     * Each call to add or remove is added to a queue of commands.
     */
    void remove( int numConnections, int port ) throws RemoteException ;
}

