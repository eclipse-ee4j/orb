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

//
// Created       : 2005 Jun 13 (Mon) 11:04:09 by Harold Carr.
// Last Modified : 2005 Aug 08 (Mon) 17:53:01 by Harold Carr.
//

package com.sun.corba.ee.spi.folb;

import java.util.List;

/**
 * @author Harold Carr
 */
public interface GroupInfoService
{
    /**
     * Adds an observer that will receive a
     * <code>membershipChange</code>
     * invocation whenever the cluster membership changes.  
     *
     * The 
     * <code>membershipChange</code>
     * invocation tells the observer to call
     * <code>getClusterInstanceInfo</code>
     * to get info.
     *
     * @param x observer to add
     * @return true if the given observer is added.  False otherwise.
     */
    public boolean addObserver(GroupInfoServiceObserver x);

    /**
     * Causes the
     * <code>membershipChange</code> 
     * method to be called on each registered observer.
     */
    public void notifyObservers();

    /**
     * This is a separate call
     * (rather than info being passed in <code>membershipChange</code>)
     * so we can identifier the adapter.
     *
     * The adapter identification is used in testing.
     * 
     * @param adapterName name of cluster
     * @return information about instances in the cluster
     */
    public List<ClusterInstanceInfo> getClusterInstanceInfo(
        String[] adapterName);

    /**
     * This is a separate call
     * (rather than info being passed in <code>membershipChange</code>)
     * so we can identifier the adapter.
     *
     * The adapter identification is used in testing.
     * @param adapterName adapter to use
     * @param endpoints  endpoints of cluster
     * @return info on cluster
     */
    public List<ClusterInstanceInfo> getClusterInstanceInfo(
        String[] adapterName, List<String> endpoints );

    /**
     * This method only used during testing.
     * @param adapterName name to add
     * @return if addresses should be added
     */
    public boolean shouldAddAddressesToNonReferenceFactory(
        String[] adapterName);

    /**
     * This method only used during testing.
     * @param adapterName name to add
     * @return if label should be added
     */
    public boolean shouldAddMembershipLabel (String[] adapterName);
}

// End of file.
