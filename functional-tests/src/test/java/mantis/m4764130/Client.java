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
// Created       : 2002 Oct 16 (Wed) 08:32:24 by Harold Carr.
// Last Modified : 2003 Mar 17 (Mon) 20:49:36 by Harold Carr.
//

package mantis.m4764130;

import java.util.Properties;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class Client 
{
    public static void main(String[] args)
    {
        try {

            Properties props = new Properties();
            props.put(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX
                      + Interceptor.class.getName(),
                      "dummy");
            ORB orb = ORB.init(args, props);

            NamingContext namingContext = 
                NamingContextHelper.narrow(orb.resolve_initial_references(
                    "NameService"));
            NameComponent nc = new NameComponent("Server", "");
            NameComponent path[] = { nc };
            Hello hello = HelloHelper.narrow(namingContext.resolve( path ));

            hello.hello("1234");

        } catch (Exception ex) {
            System.out.println("Client ERROR : " + ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }
}

// End of file.

