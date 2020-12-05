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
