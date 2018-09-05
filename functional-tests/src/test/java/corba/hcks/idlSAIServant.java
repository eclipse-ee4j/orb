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
