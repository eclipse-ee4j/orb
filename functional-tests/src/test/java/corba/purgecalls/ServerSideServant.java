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
// Created       : 2002 Jan 17 (Thu) 14:14:42 by Harold Carr.
// Last Modified : 2002 Jan 17 (Thu) 15:39:39 by Harold Carr.
//

package corba.purgecalls;

import corba.hcks.U;

class ServerSideServant
    extends
        ServerSidePOA
{
    public static final String baseMsg = ServerSideServant.class.getName();

    public ServerSideServant ( ) { }

    public void neverReturns ( )
    {
        U.sop(baseMsg + ".neverReturns");
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            U.sop(e);
        }
    }
}

// End of file.
