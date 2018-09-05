/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package test;

import java.rmi.Remote;

public interface ServantManager extends Remote {
    
    /**
     * Start a servant in the remote process.
     * @param servantClass The class of the servant object. Must have a default constructor.
     * @param servantName The name by which this servant should be known.
     * @param publishName True if the name should be published in the name server.
     * @param nameServerHost The name server host. May be null if local host.
     * @param nameServerPort The name server port.
     * @param iiop True if iiop.
     */
    public Remote startServant( String servantClass,
                                String servantName,
                                boolean publishName,
                                String nameServerHost,
                                int nameServerPort,
                                boolean iiop) throws java.rmi.RemoteException;
    
    /**
     * Unexport the specified servant. If the servant was published, will be unpublised.
     */
    public void stopServant(String servantName) throws java.rmi.RemoteException;

    /**
     * Stop all servants in this context.
     */
    public void stopAllServants() throws java.rmi.RemoteException;
    
    /**
     * @Return String the String "Pong"
     */
    public String ping() throws java.rmi.RemoteException;
}
