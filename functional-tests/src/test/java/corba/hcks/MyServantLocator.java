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
// Created       : 2000 Nov 07 (Tue) 16:29:22 by Harold Carr.
// Last Modified : 2001 Feb 07 (Wed) 16:36:40 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.IMP_LIMIT;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

public class MyServantLocator
    extends
        org.omg.CORBA.LocalObject
    implements
        ServantLocator
{
    public static final String baseMsg = MyServantLocator.class.getName();

    public ORB orb;

    public MyServantLocator(ORB orb) { this.orb = orb; }

    public Servant preinvoke(byte[] oid, POA poa, String operation,
                             CookieHolder cookieHolder)
        throws
            ForwardRequest
    {
        String soid = new String(oid);
        U.sop(baseMsg + ".preinvoke " + soid);

        if (soid.equals(C.idlAlwaysForward)) {

            throw new ForwardRequest(
               poa.create_reference_with_id(C.idlAlwaysForwardedToo.getBytes(),
                                            idlSLIHelper.id()));

        } else if (soid.startsWith("idl")) {

            // IDL.

            if (operation.equals(C.raiseForwardRequestInPreinvoke) &&
                soid.equals(C.idlSLI1)) 
            {

                throw new ForwardRequest(
                    poa.create_reference_with_id(C.idlSLI2.getBytes(),
                                                 idlSLIHelper.id()));

            } else if (operation.equals(C.raiseObjectNotExistInPreinvoke)) {

                throw new OBJECT_NOT_EXIST();

            } else if (operation.equals(C.raiseSystemExceptionInPreinvoke)) {

                throw new IMP_LIMIT();

            } else if (operation.equals(C.throwThreadDeathInPreinvoke)) {

                throw new ThreadDeath();

            }

            // Test server-side PICurrent.
            boolean ensure = false;
            if (operation.equals(C.sPic1)) {
                ensure = true;
            }
            C.testAndIncrementPICSlot(ensure, "preinvoke",
                                      SsPicInterceptor.sPic1ASlotId, 1, orb);
            C.testAndIncrementPICSlot(ensure, "preinvoke",
                                      SsPicInterceptor.sPic1BSlotId, 1, orb);

            return new idlSLIServant(orb);

        } else if (soid.startsWith("rmii")) {

            // RMII.

            return MyServantActivator.makermiiIServant(orb, soid);

        } else {

            throw new INTERNAL(U.SHOULD_NOT_SEE_THIS);

        }
    }

    public void postinvoke(byte[] oid, POA poa, String operation,
                           java.lang.Object cookie, Servant servant)
    {
        String soid = new String(oid);
        U.sop(baseMsg + ".postinvoke " + soid);
        if (operation.equals(C.raiseSystemExceptionInPostinvoke) || 
            operation.equals(C.raiseUserInServantThenSystemInPOThenSE) ||
            operation.equals(C.raiseSystemInServantThenPOThenSE))
        {

            throw new IMP_LIMIT();

        } else if (operation.equals(C.throwThreadDeathInPostinvoke)) {

            throw new ThreadDeath();

        } else if (operation.equals(C.throwThreadDeathInServantThenSysInPostThenSysInSendException))
        {

            throw new IMP_LIMIT();
        }


        // Test server-side PICurrent.
        boolean ensure = false;
        if (operation.equals(C.sPic1)) {
            ensure = true;
        }
        C.testAndIncrementPICSlot(ensure, "postinvoke",
                                  SsPicInterceptor.sPic1ASlotId, 4, orb);
        C.testAndIncrementPICSlot(ensure, "postinvoke",
                                  SsPicInterceptor.sPic1BSlotId, 4, orb);
    }
}

// End of file.
