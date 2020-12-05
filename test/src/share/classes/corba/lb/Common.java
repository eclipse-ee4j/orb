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
// Created       : 2005 Oct 05 (Wed) 14:11:24 by Harold Carr.
// Last Modified : 2005 Oct 05 (Wed) 15:07:07 by Harold Carr.
//

package corba.lb;

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
        NamingContext nc = getNameService(orb);
        nc.rebind(makeNameComponent(name), ref);
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
