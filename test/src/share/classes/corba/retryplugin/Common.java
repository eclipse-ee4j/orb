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
// Created       : 2005 Oct 05 (Wed) 14:11:24 by Harold Carr.
// Last Modified : 2005 Oct 05 (Wed) 15:07:07 by Harold Carr.
//

package corba.retryplugin;

import java.util.Vector;
import java.util.StringTokenizer;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

public class Common
{
    public static final String ReferenceName = "Test";
    private static final String NameService = "NameService";

    public static org.omg.CORBA.Object resolve(String name, ORB orb)
        throws 
            Exception
    {
        return getNameService(orb).resolve(makeNameComponent(name));
    }

    public static org.omg.CORBA.Object rebind(String name,
                                              org.omg.CORBA.Object ref,
                                              ORB orb)
        throws 
            Exception
    {
        getNameService(orb).rebind(makeNameComponent(name), ref);
        return ref;
    }

    public static NameComponent[] makeNameComponent(String name)
    {
        Vector result = new Vector();
        StringTokenizer tokens = new StringTokenizer(name, "/");
        while (tokens.hasMoreTokens()) {
            result.addElement(tokens.nextToken());
        }
        NameComponent path[] = new NameComponent[result.size()];
        for (int i = 0; i < result.size(); ++i) {
            path[i] = new NameComponent((String)result.elementAt(i), "");
        }
        return path;
    }

    public static NamingContext getNameService(ORB orb)
        throws
            Exception
    {
        return NamingContextHelper.narrow(
            orb.resolve_initial_references(NameService));
    }
}

// End of file.
