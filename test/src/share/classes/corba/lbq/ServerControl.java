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

/** Interface for a remote object used to control a server. 
 */
public interface ServerControl extends Remote {
    /** Query the server to discover its port for use in LB
     */
    int getPort() throws RemoteException ;

    /** Register the reporter with this server.  It is invoked
     * every time the server handles a request.
     */
    void registerReportCallback( ServerReporter reporter ) throws RemoteException ;

    /** Causes the server to start rejecting requests after numRequests have 
     * been handled.
     * setRejecting and clearRejecting can be queued.
     */
    void setRejecting( int numRequests ) throws RemoteException ;

    /** Causes the server to stop rejecting requests after numRequests
     * have been rejected.
     * setRejecting and clearRejecting can be queued.
     */
    void clearRejecting(int numRequests ) throws RemoteException ;
}

