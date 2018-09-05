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
// Created       : 1999 Mar 01 (Mon) 16:59:34 by Harold Carr.
// Last Modified : 2003 Dec 16 (Tue) 10:01:18 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.*;
import org.omg.CORBA.portable.UnknownException;
import org.omg.PortableServer.*;
import java.util.Properties;

class idlStaticServant
    extends 
        _idlIImplBase
{
    public static final String baseMsg = idlStaticServant.class.getName();
    public static final String from_idlStaticServant = "from idlStaticServant";

    public ORB orb;

    public idlStaticServant(ORB orb)
    {
        this.orb = orb;
    }

    public String syncOK(String arg1)
    {
        return baseMsg + " " + arg1;
    }

    public synchronized void asyncOK(byte[] data)
    {
        try {
            U.sop(new String(data, C.UTF8));
        } catch (Exception e) {
            U.sopUnexpectedException(baseMsg + C.asyncOK, e);
        }
    }

    public void throwUserException()
        throws idlExampleException
    {
        C.throwUserException(from_idlStaticServant);
    }

    public void throwSystemException()
    {
        C.throwSystemException(from_idlStaticServant);
    }

    public void throwUnknownException()
    {
        C.throwUnknownException(from_idlStaticServant);
    }

    public void throwUNKNOWN()
    {
        C.throwUNKNOWN(from_idlStaticServant);
    }

    public void raiseSystemExceptionInSendReply()
    {
    }

    public void testEffectiveTarget1()
    {
    }

    public void testEffectiveTarget2()
    {
    }

    public String testMonitoring ()
    {
        return "";
    }

    public idlValueTypeA sendValue (idlValueTypeA a, 
                                    idlValueTypeB b, 
                                    idlValueTypeC c,
                                    idlValueTypeD d,
                                    idlValueTypeE e,
                                    int[]         f,
                                    byte[]        g)
    {
        U.sop(d);
        return b;
    }

    public org.omg.CORBA.Object getAndSaveUnknownORBVersionIOR()
    {
        throw new RuntimeException("Not implemented");
    }

    public boolean isIdenticalWithSavedIOR(org.omg.CORBA.Object o)
    {
        throw new RuntimeException("Not implemented");
    }
}

// End of file.

