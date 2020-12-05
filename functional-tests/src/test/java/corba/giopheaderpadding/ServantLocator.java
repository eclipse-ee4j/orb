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
// Last Modified : 2004 Jan 31 (Sat) 11:13:27 by Harold Carr.
//

package corba.giopheaderpadding;

import org.omg.CORBA.ORB;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

import corba.hcks.U;

public class ServantLocator extends org.omg.CORBA.LocalObject
    implements org.omg.PortableServer.ServantLocator {

    public static final String baseMsg = ServantLocator.class.getName();

    public ServantLocator() {}

    public Servant preinvoke(byte[] oid, POA poa, String operation,
                             CookieHolder cookieHolder)
        throws ForwardRequest {

        String soid = new String(oid);
        U.sop(baseMsg + ".preinvoke " + soid);

        Servant servant = null;
        try {
            servant = 
                (Servant)javax.rmi.CORBA.Util.getTie(new rmiiIServantPOA());
        } catch (Exception e) {
            U.sopUnexpectedException(baseMsg, e);
        }
        return servant;
    }

    public void postinvoke(byte[] oid, POA poa, String operation,
                           java.lang.Object cookie, Servant servant)
    {
    }
}

// End of file.
