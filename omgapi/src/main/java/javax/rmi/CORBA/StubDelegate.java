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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import org.omg.CORBA.ORB;

/**
 * Supports delegation for method implementations in {@link Stub}.
 * A delegate is an instance of a class that implements this
 * interface and provides a replacement implementation for all the
 * methods of <code>javax.rmi.CORBA.Stub</code>.  If delegation is
 * enabled, each stub has an associated delegate.
 *
 * Delegates are enabled by providing the delegate's class name as the
 * value of the 
 * <code>javax.rmi.CORBA.StubClass</code>
 * system property.
 *
 * @see Stub
 */
public interface StubDelegate {

    /**
     * Delegation call for {@link Stub#hashCode}.
     * @param self stub to call on
     * @return int hashcode of the stub
     */
    int hashCode(Stub self);

    /**
     * Delegation call for {@link Stub#equals}.
     * @param self stub to call on
     * @param obj other object to compare
     * @return true if the two objects are equal
     */
    boolean equals(Stub self, java.lang.Object obj);

    /**
     * Delegation call for {@link Stub#toString}.
     * @param self to call toString on
     * @return String representation of the Stub
     */
    String toString(Stub self);

    /**
     * Delegation call for {@link Stub#connect}.
     * @param self stub to call on
     * @param orb the ORB to connect to
     * @throws RemoteException if there was an error connecting
     */
    void connect(Stub self, ORB orb)
        throws RemoteException;
 
    // _REVISIT_ cannot link to Stub.readObject directly... why not?
    /**
     * Delegation call for
     * <a href="{@docRoot}/serialized-form.html#javax.rmi.CORBA.Stub"><code>Stub.readObject(java.io.ObjectInputStream)</code></a>.
     * @param self stub to read
     * @param s stream to read from
     * @throws IOException if there was an error reading from the stream
     * @throws ClassNotFoundException if the class that was represented by the steam cannot be found
     */
    void readObject(Stub self, ObjectInputStream s)
        throws IOException, ClassNotFoundException;

    // _REVISIT_ cannot link to Stub.writeObject directly... why not?
    /**
     * Delegation call for 
     * <a href="{@docRoot}/serialized-form.html#javax.rmi.CORBA.Stub"><code>Stub.writeObject(java.io.ObjectOutputStream)</code></a>.
     * @param self stub to write
     * @param s stream to write to
     * @throws IOException if there was an error writing to stream
     */
    void writeObject(Stub self, ObjectOutputStream s)
        throws IOException;

}
