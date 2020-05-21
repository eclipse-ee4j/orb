/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.glassfish.jndi.cosnaming;

import org.omg.CORBA.ORB;

/**
 * This class keeps track of references to the shared ORB object
 * and destroys it when no more references are made to the ORB
 * object. This object is created for each ORB object that CNCtx
 * creates.
 */
class OrbReuseTracker {

    int referenceCnt;
    ORB orb;

    private static final boolean debug = false;

    OrbReuseTracker(ORB orb) {
        this.orb = orb;
        referenceCnt++;
        if (debug) {
             System.out.println("New OrbReuseTracker created");
        }
    }

    synchronized void incRefCount() {
        referenceCnt++;
        if (debug) {
             System.out.println("Increment orb ref count to:" + referenceCnt);
        }
    }

    synchronized void decRefCount() {
        referenceCnt--;
        if (debug) {
             System.out.println("Decrement orb ref count to:" + referenceCnt);
        }
        if ((referenceCnt == 0)) {
            if (debug) {
                System.out.println("Destroying the ORB");
            }
            orb.destroy();
        }
    }
}
