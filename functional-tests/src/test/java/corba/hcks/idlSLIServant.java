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
// Created       : 2000 Nov 07 (Tue) 12:16:39 by Harold Carr.
// Last Modified : 2001 Feb 07 (Wed) 17:19:31 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.ORB;
import org.omg.CORBA.NO_MEMORY;

class idlSLIServant
    extends 
        idlSLIPOA
{
    public static String baseMsg = idlSLIServant.class.getName();

    public ORB orb;

    public idlSLIServant ( ORB orb ) { this.orb = orb; }
    public String raiseForwardRequestInPreinvoke ( String a ) { return a; }
    public String raiseObjectNotExistInPreinvoke ( String a ) { return a; }
    public String raiseSystemExceptionInPreinvoke ( String a ) { return a; }
    public String raiseSystemExceptionInPostinvoke ( String a ) { return a; }

    public String raiseSystemInServantThenPOThenSE ( )
    {
        throw new NO_MEMORY();
    }

    public String raiseUserInServantThenSystemInPOThenSE ( )
        throws 
            idlExampleException
    {
        C.throwUserException(baseMsg +
                             C.raiseUserInServantThenSystemInPOThenSE);
        // return for compiler
        return U.SHOULD_NOT_SEE_THIS;
    }

    public String makeColocatedCallFromServant ( )
    {
        return C.makeColocatedCallFromServant(C.idlSLI1, orb, baseMsg);
    }
    public String colocatedCallFromServant ( String a )
    {
        return C.colocatedCallFromServant(a, orb, baseMsg);
    }

    public String throwThreadDeathInReceiveRequestServiceContexts( String a )
    {
        U.sop(U.servant(a));
        return a; 
    }
    public String throwThreadDeathInPreinvoke ( String a )
    {
        U.sop(U.servant(a));
        return a; 
    }
    public String throwThreadDeathInReceiveRequest ( String a )
    {
        U.sop(U.servant(a));
        return a;
    }

    public String throwThreadDeathInServant ( String a )
    {
        U.sop(U.servant(a));
        throw new ThreadDeath();
    }
    public String throwThreadDeathInPostinvoke ( String a ) 
    {
        U.sop(U.servant(a));
        return a; 
    }
    public String throwThreadDeathInSendReply ( String a ) 
    { 
        U.sop(U.servant(a));
        return a; 
    }
    public String throwThreadDeathInServantThenSysInPostThenSysInSendException ( String a )
    {
        U.sop(U.servant(a));
        throw new ThreadDeath();
    }

    public void sPic1()
    {
        C.testAndIncrementPICSlot(true, C.sPic1,
                                  SsPicInterceptor.sPic1ASlotId, 3, orb);
        C.testAndIncrementPICSlot(true, C.sPic1,
                                  SsPicInterceptor.sPic1BSlotId, 3, orb);
    }
    public void sPic2()
    {
    }
}

// End of file.
