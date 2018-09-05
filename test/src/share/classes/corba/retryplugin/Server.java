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
// Created       : 2005 Oct 05 (Wed) 13:54:09 by Harold Carr.
// Last Modified : 2005 Oct 06 (Thu) 11:32:08 by Harold Carr.
//

package corba.retryplugin;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import com.sun.corba.ee.impl.plugin.hwlb.RetryServerRequestInterceptor ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

/**
 * @author Harold Carr
 */
public class Server
{
    static {
        // This is needed to guarantee that this test will ALWAYS use dynamic
        // RMI-IIOP.  Currently the default is dynamic when renamed to "ee",
        // but static in the default "se" packaging, and this test will
        // fail without dynamic RMI-IIOP.
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;
    }

    private static final String baseMsg = Server.class.getName();
    private static final String RootPOA = "RootPOA";
    private static final long SERVER_RUN_LENGTH = 1000 * 60; // 1 minute

    public static void main(String[] av)
    {
        try {

            Properties props = new Properties();

            props.setProperty(
                "org.omg.PortableInterceptor.ORBInitializerClass."
                + RetryServerRequestInterceptor.class.getName(),
                "dummy");

            ORB orb = ORB.init(av, props);

            POA rootPOA = (POA) orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();

            Servant servant = (Servant)
                javax.rmi.CORBA.Util.getTie(new TestServant());

            createWithServantAndBind(Common.ReferenceName, servant, 
                                     rootPOA, orb);

            System.out.println("--------------------------------------------");
            System.out.println("Server is ready.");
            System.out.println("--------------------------------------------");

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < SERVER_RUN_LENGTH){
                RetryServerRequestInterceptor.setRejectingRequests(true);
                Thread.sleep(1000 * 10);
                RetryServerRequestInterceptor.setRejectingRequests(false);
                Thread.sleep(1000 * 10);
            }

            RetryServerRequestInterceptor.setRejectingRequests(true);
            Thread.sleep(SERVER_RUN_LENGTH);

            System.out.println("--------------------------------------------");
            System.out.println("Server exiting correctly...");
            System.out.println("--------------------------------------------");
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("--------------------------------------------");
            System.out.println("!!!! Server exiting INCORRECTLY...");
            System.out.println("--------------------------------------------");
            System.exit(1);
        }
    }

    public static org.omg.CORBA.Object 
        createWithServantAndBind (String  name,
                                  Servant servant,
                                  POA     poa,
                                  ORB     orb)
        throws
            Exception
    {
        byte[] id = name.getBytes();
        poa.activate_object_with_id(id, servant);
        org.omg.CORBA.Object ref = poa.id_to_reference(id);
        Common.rebind(name, ref, orb);
        return ref;
    }

} 

// End of file.
