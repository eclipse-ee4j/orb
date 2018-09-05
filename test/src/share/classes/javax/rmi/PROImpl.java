/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.rmi;

import com.sun.corba.ee.spi.JndiConstants;

import javax.naming.InitialContext;
import java.rmi.RemoteException;

public class PROImpl extends PortableRemoteObject implements PROHello {
    
    public PROImpl () throws RemoteException {
        super();
    }
    
    public String sayHello () throws RemoteException {
        return HELLO;
    }
    
    public Dog getDogValue () throws RemoteException {
        return new DogImpl ("Bow wow!");
    }

    public Dog getDogServer () throws RemoteException {
        return new DogServer ("Yip Yip Yip!");
    }

    public void unexport () throws RemoteException {
        PortableRemoteObject.unexportObject(this);
    }
    
    private static InitialContext context ;

    public static void main (String[] args) {
        
        // args[0] == 'iiop' || 'jrmp'
        // args[1] == publishName
        
        try {
          
            if (args[0].equalsIgnoreCase("iiop")) {
                System.getProperties().put("java.naming.factory.initial", JndiConstants.COSNAMING_CONTEXT_FACTORY);
            } else if (args[0].equalsIgnoreCase("jrmp")) {
                System.getProperties().put("java.naming.factory.initial", JndiConstants.REGISTRY_CONTEXT_FACTORY);
            }
            
            context = new InitialContext ();
            context.rebind (args[1], new PROImpl());
          
        } catch (Exception e) {
            System.out.println ("Caught: " + e.getMessage());
        }
    }
}
