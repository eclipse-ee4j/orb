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
// Created       : 2002 Jul 19 (Fri) 14:47:13 by Harold Carr.
// Last Modified : 2004 Jun 06 (Sun) 12:21:47 by Harold Carr.
//

package corba.iorintsockfact;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;

import com.sun.corba.ee.spi.misc.ORBConstants;

/**
 * @author Harold Carr
 */
public abstract class Common
{
    public static final String SOCKET_FACTORY_CLASS_PROPERTY =
        ORBConstants.LEGACY_SOCKET_FACTORY_CLASS_PROPERTY;

    public static final String CUSTOM_FACTORY_CLASS =
        SocketFactory.class.getName();

    public static final String serverName1 = "I1";

    public static NamingContext getNameService(ORB orb)
    {
        org.omg.CORBA.Object objRef = null;
        try {
            objRef = orb.resolve_initial_references("NameService");
        } catch (Exception ex) {
            System.out.println("Common.getNameService: " + ex);
            ex.printStackTrace(System.out);
            System.exit(-1);
        }
        return NamingContextHelper.narrow(objRef);
    }

    public static NameComponent[] makeNameComponent(String name)
    {
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
        return path;
    }
}

// End of file.

