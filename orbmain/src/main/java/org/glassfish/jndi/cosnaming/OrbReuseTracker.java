/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jndi.cosnaming;

import org.omg.CORBA.ORB;

/**
 * This class keeps track of references to the shared ORB object and destroys it when no more references are made to the
 * ORB object. This object is created for each ORB object that CNCtx creates.
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
