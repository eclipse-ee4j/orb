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

package javax.rmi.CORBA;

import java.rmi.Remote;
import java.util.Hashtable;

import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.ORB;

/**
 * Defines methods which all RMI-IIOP server side ties must implement.
 */
public interface Tie extends org.omg.CORBA.portable.InvokeHandler {
    /**
     * Returns an object reference for the target object represented by this tie.
     *
     * @return an object reference for the target object.
     */
    org.omg.CORBA.Object thisObject();

    /**
     * Deactivates the target object represented by this tie.
     */
    void deactivate() throws java.rmi.NoSuchObjectException;

    /**
     * Returns the ORB for this tie.
     *
     * @return the ORB.
     */
    ORB orb();

    /**
     * Sets the ORB for this tie.
     *
     * @param orb the ORB.
     */
    void orb(ORB orb);

    /**
     * Called by {@link Util#registerTarget} to set the target for this tie.
     *
     * @param target the object to use as the target for this tie.
     */
    void setTarget(java.rmi.Remote target);

    /**
     * Returns the target for this tie.
     *
     * @return the target.
     */
    java.rmi.Remote getTarget();
}
