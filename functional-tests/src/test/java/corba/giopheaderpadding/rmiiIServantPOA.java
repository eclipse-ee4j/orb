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
// Last Modified : 2004 Jan 31 (Sat) 09:56:52 by Harold Carr.
//

package corba.giopheaderpadding;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;
import corba.hcks.U;

public class rmiiIServantPOA extends PortableRemoteObject 
    implements rmiiI {

    public rmiiIServantPOA() throws RemoteException { 
        // DO NOT CALL SUPER - that would connect the object.
    }

    public byte fooA(byte x) {
        U.sop(x + "");
        return x;
    }

    public void fooB() {
    }
}

// End of file.
