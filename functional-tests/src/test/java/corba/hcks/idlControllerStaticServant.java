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
// Created       : 2001 Feb 23 (Fri) 07:12:06 by Harold Carr.
// Last Modified : 2001 Feb 23 (Fri) 14:46:31 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.ORB;

class idlControllerStaticServant
    extends 
        _idlControllerIImplBase 
{
    public static final String baseMsg =
        idlControllerStaticServant.class.getName();

    public ORB ridlStaticORB;

    public void setRidlStaticORB (ORB ridlStaticORB)
    {
        this.ridlStaticORB = ridlStaticORB;
    }

    public String action (String action)
    {
        if (action.equals(C.disconnectRidlStaticServant)) {

            ridlStaticORB.disconnect(Server.ridlStaticForDisconnect);

        } else {

            U.sopShouldNotSeeThis(action);
            return U.SHOULD_NOT_SEE_THIS;

        }
        return action;
    }
}

// End of file.

