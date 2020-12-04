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
// Created       : 2001 Nov 10 (Sat) 12:50:19 by Harold Carr.
// Last Modified : 2001 Nov 10 (Sat) 12:59:46 by Harold Carr.
//

package corba.hcks;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;

public class rmiiAttributesIServantPOA
    extends 
        PortableRemoteObject
    implements
        rmiiAttributesI
{
    public rmiiAttributesIServantPOA()
        throws
            RemoteException {}

    public Integer getInteger()
        throws
            RemoteException { return null; }

    public void    setInteger(Integer x)
        throws
            RemoteException { }

    public boolean isTrue()
        throws
            RemoteException { return true; }

    public boolean getTrue()
        throws
            RemoteException { return true; }

    public void    setTrue(boolean x)
        throws
            RemoteException { }

    // Test that this is NOT a JavaBeans pattern.
    public Integer get()
        throws
            RemoteException { return null; }

    public void set(Integer x)
        throws
            RemoteException { }

}

// End of file.
