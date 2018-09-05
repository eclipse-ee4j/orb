/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2000 Nov 11 (Sat) 10:45:48 by Harold Carr.
// Last Modified : 2003 Apr 18 (Fri) 15:20:47 by Harold Carr.
//

package corba.islocal;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;
import corba.hcks.U;

public class rmiiIServantPOA
    extends 
        PortableRemoteObject
    implements
        rmiiI
{
    public static final String baseMsg = rmiiIServantPOA.class.getName();

    public rmiiIServantPOA ()
        throws
            RemoteException
    { 
        // DO NOT CALL SUPER - that would connect the object.
    }

    public String m(String x)
    {
        Server.checkThread(baseMsg);
        String result = Server.filter(x, baseMsg);
        U.sop(result);
        return result;
    }
}

// End of file.
