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
// Created       : 2003 May 18 (Sun) 22:16:39 by Harold Carr.
// Last Modified : 2003 May 20 (Tue) 07:50:28 by Harold Carr.
//

package corba.islocal;

import javax.rmi.CORBA.Tie;

import org.omg.CORBA.IMP_LIMIT;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

import corba.hcks.U;

public class MyServantLocator
    extends
        org.omg.CORBA.LocalObject
    implements
        ServantLocator
{
    public static final String baseMsg = MyServantLocator.class.getName();
    public static final String thisPackage = 
        MyServantLocator.class.getPackage().getName();


    public ORB orb;

    public MyServantLocator(ORB orb) { this.orb = orb; }

    public Servant preinvoke(byte[] oid, POA poa, String operation,
                             CookieHolder cookieHolder)
        throws
            ForwardRequest
    {
        ClassLoader classLoader      = null;
        Class rmiiIServantPOAClass   = null;
        Object rmiiIServantPOAObject = null;
        Tie tie                      = null;
        try {
            classLoader = new CustomClassLoader();
            rmiiIServantPOAClass = 
                classLoader.loadClass(thisPackage + ".rmiiIServantPOA");
            rmiiIServantPOAObject = rmiiIServantPOAClass.newInstance();
            classLoader = rmiiIServantPOAObject.getClass().getClassLoader();
            System.out.println("rmiiIServantPOAClass: "
                               + rmiiIServantPOAClass);
            System.out.println("rmiiIServantPOAObject classLoader: " +
                               classLoader);
            System.out.println("rmiiIServantPOAObject: " +
                               rmiiIServantPOAObject);
            //tie = javax.rmi.CORBA.Util.getTie(rmiiIServantPOAObject);
            tie = (Tie) Class.forName(thisPackage + "._rmiiIServantPOA_Tie")
                .newInstance();
            reflect(tie.getClass());
            reflect(java.rmi.Remote.class);
            reflect(rmiiIServantPOAObject.getClass());
            tie.setTarget((java.rmi.Remote)rmiiIServantPOAObject);
            return (Servant) tie;
        } catch (Throwable t) {
            U.sopUnexpectedException("preinvoke", t);
            System.exit(-1);
        }
        return null;
    }

    public void postinvoke(byte[] oid, POA poa, String operation,
                           java.lang.Object cookie, Servant servant)
    {
    }

    private void reflect(Class c)
    {
        reflect(c, 0);
    }

    private void reflect(Class c, int indent)
    {
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
        System.out.println(c + " " + c.getClassLoader());

        Class[] interfaces = c.getInterfaces();
        for (int j = 0; j < interfaces.length; j++) {
            reflect(interfaces[j], indent + 2);
        }
        if (c.getSuperclass() != null) {
            reflect(c.getSuperclass(), indent + 2);
        }
    }
}

// End of file.

