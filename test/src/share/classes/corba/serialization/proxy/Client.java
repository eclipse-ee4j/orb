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
