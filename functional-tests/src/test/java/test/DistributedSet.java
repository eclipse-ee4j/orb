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
import java.rmi.RemoteException;

/**
 * DistributedSet is a remote interface which supports a distributed
 * collection of sets, all of which coordinate with each other to maintain,
 * in each instance, a list of all the currently active sets.
 *
 * @version     1.0, 5/13/98
 * @author      Bryan Atsatt
 */
public interface DistributedSet extends Remote {

    public static final String PING_RESPONSE = "Pong";

    /*
     * See if this set is still active. Returns PING_RESPONSE.
     */
    public String ping (String fromSetName) throws RemoteException;

    /*
     * Get this set's name.
     */
    public String getName () throws RemoteException;
    
    /*
     * Notify this set that the specified set is joining. If the set
     * already is 'known' by this instance, this call performs no
     * action.
     */
    public void join (String setName, DistributedSet set) throws RemoteException;
    
    /*
     * Notify this set that the specified set is leaving.
     */
    public void leave (String setName) throws RemoteException;
    
    /*
     * Broadcast a message to all sets.
     */
    public void broadcastMessage (String message) throws RemoteException;
    
    /*
     * Send a message to specified set.
     */
    public void sendMessage (DistributedSet toSet, String message) throws RemoteException;
    
    /*
     * Receive a message from another set.
     */
    public void receiveMessage (String message, String fromSetName) throws RemoteException;
    
    /*
     * Return the number of currently active sets, _excluding_ 
     * this instance.
     */
    public int countSets () throws RemoteException;
    
    /*
     * List the names of all the active sets, _excluding_ this
     * instance.
     */
    public String[] listSetNames () throws RemoteException;
    
    /*
     * Get a set instance by name. Returns null if not found.
     */
    public DistributedSet getSet (String setName) throws RemoteException;
}

