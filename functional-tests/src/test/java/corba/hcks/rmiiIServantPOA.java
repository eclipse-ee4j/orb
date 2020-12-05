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
// Created       : 2000 Nov 11 (Sat) 10:45:48 by Harold Carr.
// Last Modified : 2001 May 10 (Thu) 15:45:53 by Harold Carr.
//

package corba.hcks;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;

public class rmiiIServantPOA
    extends 
        PortableRemoteObject
    implements
        rmiiI
{
    public static final String baseMsg = rmiiIServantPOA.class.getName();

    public ORB orb;
    public String name;

    public rmiiIServantPOA (ORB orb, String name)
        throws
            RemoteException
    { 
        // DO NOT CALL SUPER - that would connect the object.
        this.orb = orb; 
        this.name = name;
    }

    public String sayHello ()
    {
        return C.helloWorld;
    }

    public int sendBytes (byte[] x)
    {
        if (x == null)
            return -1;
        return x.length;
    }

    public Object sendOneObject (Object x)
        throws
            rmiiMyException
    {
        return x;
    }

    public Object sendTwoObjects (Object x, Object y)
    {
        return x;
    }

    public String makeColocatedCallFromServant ()
        throws
            RemoteException
    {
        rmiiI rrmiiI = null;
        String result = "";
        try {
            result = new String(U.getPOACurrentOperation(orb));

            // Colocated via narrow.

            rrmiiI = null;
            rrmiiI = (rmiiI) this.narrow(this, rmiiI.class);
            result = doCall(rrmiiI, result);

            // Colocated via PortableRemoteObject.narrow

            rrmiiI = null;
            rrmiiI = (rmiiI) PortableRemoteObject.narrow(this, rmiiI.class);
            result = doCall(rrmiiI, result);

            // Colocated via common context and PRO.narrow

            rrmiiI = null;
            rrmiiI = (rmiiI)
                U.lookupAndNarrow(name, rmiiI.class, Server.initialContext);
            result = doCall(rrmiiI, result);

        } catch (Exception e) {
            U.sopUnexpectedException(baseMsg + C.makeColocatedCallFromServant,
                                     e);
            INTERNAL exc = new INTERNAL(U.SHOULD_NOT_SEE_THIS);
            exc.initCause( e ) ;
            throw exc ;
        }
        return result;
    }

    private String doCall(rmiiI rrmiiI, String resultSoFar)
        throws
            Exception
    {
        String result = rrmiiI.colocatedCallFromServant(resultSoFar);
        String op = new String(U.getPOACurrentOperation(orb));
        return op + " " + result;
    }

    public String colocatedCallFromServant (String a)
        throws
            RemoteException,
            Exception
    {
        String op = new String(U.getPOACurrentOperation(orb));
        return op + " " + a;
    }

    public String throwThreadDeathInServant (String a)
        throws
            RemoteException,
            ThreadDeath
    {
        U.sop(U.servant(a));
        throw new ThreadDeath();
    }

    public Object returnObjectFromServer (boolean isSerializable)
        throws
            RemoteException
    {
        if (isSerializable) {
            return new SerializableObject();
        } else {
            return new NonSerializableObject();
        }
    }
}

// End of file.
