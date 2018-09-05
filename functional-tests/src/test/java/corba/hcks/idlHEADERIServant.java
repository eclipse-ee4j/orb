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
// Created       : 2000 Nov 08 (Wed) 11:23:52 by Harold Carr.
// Last Modified : 2000 Nov 25 (Sat) 13:11:30 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.ORB;

class idlHEADERIServant
    extends
        idlHEADERIPOA
{
    private ORB orb;
    public idlHEADERIServant ( ORB orb ) { this.orb = orb; }
    public void HEADER ( String message )
    {
        U.HEADER(message);
    }
}

// End of file.
