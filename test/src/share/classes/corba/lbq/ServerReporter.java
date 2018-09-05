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

/** One of these is implemented by Orchestrator for each server instance and
 * registered with the server.
 */
public interface ServerReporter extends Remote {
    /** Report that the server is about to responds to a request
     * with the given requestNumber as its argument.
     */
    void requestReceived( int requestNumber ) throws RemoteException ;
}

