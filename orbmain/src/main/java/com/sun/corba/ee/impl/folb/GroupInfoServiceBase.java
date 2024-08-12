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
// Last Modified : 2005 Aug 09 (Tue) 16:31:38 by Harold Carr.
//

package com.sun.corba.ee.impl.folb;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;

import com.sun.corba.ee.spi.trace.Folb;

import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.spi.folb.GroupInfoServiceObserver;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/**
 * @author Harold Carr
 */
@Folb
public abstract class GroupInfoServiceBase extends org.omg.CORBA.LocalObject implements GroupInfoService {
    private List<GroupInfoServiceObserver> observers = new LinkedList<GroupInfoServiceObserver>();

    @Folb
    public boolean addObserver(GroupInfoServiceObserver x) {
        return observers.add(x);
    }

    @InfoMethod
    private void observerInfo(GroupInfoServiceObserver obs) {
    }

    @Folb
    public void notifyObservers() {
        for (GroupInfoServiceObserver observer : observers) {
            observerInfo(observer);
            observer.membershipChange();
        }
    }

    @Folb
    public List<ClusterInstanceInfo> getClusterInstanceInfo(String[] adapterName) {

        // Make a copy of the internal data
        return new ArrayList(internalClusterInstanceInfo());
    }

    public List<ClusterInstanceInfo> getClusterInstanceInfo(String[] adapterName, List<String> endpoints) {

        // Make a copy of the internal data
        return new ArrayList(internalClusterInstanceInfo(endpoints));
    }

    @Folb
    public boolean shouldAddAddressesToNonReferenceFactory(String[] adapterName) {
        return false;
    }

    @Folb
    public boolean shouldAddMembershipLabel(String[] adapterName) {
        return true;
    }

    public List<ClusterInstanceInfo> internalClusterInstanceInfo() {
        final List<String> endpoints = new ArrayList<String>();
        return internalClusterInstanceInfo(endpoints);
    }

    public abstract List<ClusterInstanceInfo> internalClusterInstanceInfo(List<String> endpoints);
}

// End of file.
