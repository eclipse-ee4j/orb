/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package javax.rmi.CORBA;

import java.rmi.NoSuchObjectException;

import org.omg.CORBA.ORB;

/**
 * Defines methods which all RMI-IIOP server side ties must implement.
 */
public interface Tie extends org.omg.CORBA.portable.InvokeHandler {
    /**
     * Returns an object reference for the target object represented by
     * this tie.
     * @return an object reference for the target object.
     */
    org.omg.CORBA.Object thisObject();
    
    /**
     * Deactivates the target object represented by this tie.
     * @throws NoSuchObjectException if this tie does not represent an object
     */
    void deactivate() throws NoSuchObjectException;
    
    /**
     * Returns the ORB for this tie.
     * @return the ORB.
     */
    ORB orb();
    
    /**
     * Sets the ORB for this tie.
     * @param orb the ORB.
     */
    void orb(ORB orb);
    
    /**
     * Called by {@link Util#registerTarget} to set the target
     * for this tie.
     * @param target the object to use as the target for this tie.
     */
    void setTarget(java.rmi.Remote target);
   
    /**
     * Returns the target for this tie.
     * @return the target.
     */
    java.rmi.Remote getTarget();
}
