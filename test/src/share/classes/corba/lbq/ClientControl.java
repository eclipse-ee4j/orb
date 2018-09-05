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

/** Interface for remote object used to control the Client behavior.
 */
public interface ClientControl extends Remote {
    /** Tell the Client to send the given number of requests.
     * The client sends the requests as fast as possible after
     * this method returns.
     */
    void startSendingRequests( int numRequests ) throws RemoteException ;
}

