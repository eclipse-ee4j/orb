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
// Created       : 2000 Nov 08 (Wed) 09:45:40 by Harold Carr.
// Last Modified : 2000 Nov 26 (Sun) 20:22:32 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.ORB;

class idlSAIServant
    extends 
        idlSAIPOA
{
    public ORB orb;
    public static String clazz = "idlSAIServant";
    
    public idlSAIServant ( ORB orb ) { this.orb = orb; }
    public String raiseForwardRequestInIncarnate ( String a ) { return a; }
    public String raiseObjectNotExistInIncarnate ( String a ) { return a; }
    public String raiseSystemExceptionInIncarnate ( String a ) { return a; }
    public String makeColocatedCallFromServant ( )
    {
        return C.makeColocatedCallFromServant(C.idlSAI2, orb, clazz);
    }
    public String colocatedCallFromServant ( String a )
    {
        return C.colocatedCallFromServant(a, orb, clazz);
    }
}

// End of file.
