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
// Created       : 2001 May 23 (Wed) 11:57:05 by Harold Carr.
// Last Modified : 2001 Sep 20 (Thu) 21:39:43 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;

class AServiceImpl
    extends LocalObject
    implements AService
{
    private int slotId;

    private int currentServiceId = 0;

    private Current piCurrent;

    private Any NOT_IN_EFFECT;

    public AServiceImpl(int slotId)
    {
        this.slotId = slotId;
        NOT_IN_EFFECT = ORB.init().create_any();
    }

    // Package protected so the AService ORBInitializer can access this
    // non-IDL defined method.
    void setPICurrent(Current piCurrent)
    {
        this.piCurrent = piCurrent;
    }

    public void begin()
    {
        Any any = ORB.init().create_any();
        any.insert_long(++currentServiceId);
        setSlot(any);
    }

    public void end()
    {
        setSlot(NOT_IN_EFFECT);
    }

    public void verify()
    {
        try {
            Any any = piCurrent.get_slot(slotId);
            if (any.type().kind().equals(TCKind.tk_long)) {
                System.out.println("Service present: " + any.extract_long());
            } else {
                System.out.println("Service not present");
            }
        } catch (InvalidSlot e) {
            System.out.println("Exception handling not shown.");
        }
    }

    // Synchronized because two threads in the same ORB could be
    // sharing this object.
    synchronized private void setSlot(Any any)
    {
        try {
            piCurrent.set_slot(slotId, any);
        } catch (InvalidSlot e) {
            System.out.println("Exception handling not shown.");
        }
    }
}

// End of file.

