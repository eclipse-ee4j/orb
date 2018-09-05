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
// Created       : 2004 Aug 12 (Thu) 14:06:19 by Harold Carr.
// Last Modified : 2005 Jun 01 (Wed) 11:07:40 by Harold Carr.
//

package corba.folb_8_1;


import java.util.Properties ;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;



/**
 * @author Harold Carr
 */
public class ZeroPortServer1
{
    public static final String baseMsg = Common.class.getName();

    public static ORB orb;

    public static String serverName = Common.zero1;
    public static int[]  socketPorts = Common.socketPorts;

    public static void main(String av[])
    {
        try {
            Properties props = System.getProperties();
            Server.setProperties(props, socketPorts);
            orb = ORB.init(av, props);

            POA poa = Common.createPOA("zeroPortPOA", true, orb);
            Common.createAndBind(serverName, orb, poa);
      
            System.out.println ("Server is ready.");

            orb.run();
            
        } catch (Exception e) {
            System.out.println(baseMsg + e);
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
}

// End of file.
