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
