/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.serialization.proxy;

import java.rmi.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.*;

import java.io.*;

public class Client 
{
    public static void main(String args[])
    {
        FrobnicatorProvider test = null;
        try {
            System.setSecurityManager(new NoSecurityManager());
            Context initialNamingContext = new InitialContext();
            Object myLook = initialNamingContext.lookup("DynamicProxyBug1368");

            //System.out.println("Lookup = " + initialNamingContext );
            //System.out.println("LookObjType = " + myLook.getClass().getName() );

            //Obtain a stub for the remote object.
            test = (FrobnicatorProvider)PortableRemoteObject.narrow(
                             myLook ,
                             FrobnicatorProvider.class);
            Frobnicator frobnicator = test.getFrobnicator();
            //toString operation will cause proxy invocation
            //System.out.println("My frob= " + frobnicator);
            //System.out.println("remoting..");
            frobnicator.frobnicate();
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("Error: DynamicProxyBug1368 Failed");
            System.exit(1);
        }
    }
}
