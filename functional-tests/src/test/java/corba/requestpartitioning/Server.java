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

package corba.requestpartitioning;

import java.util.Properties;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.extension.RequestPartitioningPolicy;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;

import corba.framework.Options;
import corba.hcks.U;

import org.omg.CORBA.Policy;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

//
// Created      : 2004 June 2, 2004 by Charlie Hunt
// Last Modified: 2004 June 2, 2004 by Charlie Hunt
//

public class Server
{
    private static ORB orb = null;

    public static void main(String[] args)
    {
        Properties props = System.getProperties();
        try
        {
            orb = (ORB)org.omg.CORBA.ORB.init(args, props);

            // set custom thread pool manager
            ThreadPoolManager threadPoolManager =
                          TestThreadPoolManager.getThreadPoolManager();
            orb.setThreadPoolManager(threadPoolManager);

            // Get a reference to rootpoa
            POA rootPOA = POAHelper.narrow(
                   orb.resolve_initial_references(ORBConstants.ROOT_POA_NAME)); 

            // Create servant and register it with the ORB
            TesterImpl testerImpl = new TesterImpl();

            U.sop("Creating a request partitioning policy with -1...");
            try {
                Policy policy[] = new Policy[1];
                policy[0] = new RequestPartitioningPolicy(-1);
                throw new Exception("new RequestPartitionPolicy(-1) was not rejected when it should have been!");
            }
            catch (Exception ex) {
                U.sop("Received expected exception...");
            }
            U.sop("Creating a request partitioning policy with 64...");
            try {
                Policy policy[] = new Policy[1];
                policy[0] = new RequestPartitioningPolicy(64);
                throw new Exception("new RequestPartitionPolicy(64) was not rejected when it should have been!");
            }
            catch (Exception ex) {
                U.sop("Received expected exception...");
            }

            org.omg.CORBA.Object objRef =
                             orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            NameComponent[] path = null;

            POA[] poa = new POA[TestThreadPoolManager.NUMBER_OF_THREAD_POOLS_TO_CREATE];
            Policy policy[] = new Policy[1];

            for (int i = 0; i < poa.length; i++) {
                policy[0] = new RequestPartitioningPolicy(i);
                String poaName = "POA-Tester" + i;
                poa[i] = rootPOA.create_POA(poaName, null, policy);
                poa[i].activate_object(testerImpl);

                org.omg.CORBA.Object ref = 
                       poa[i].servant_to_reference(testerImpl);
                Tester testerRef = TesterHelper.narrow(ref);

                String name = "Tester" + i;
                path = ncRef.to_name(name);
                ncRef.rebind(path, testerRef);

                poa[i].the_POAManager().activate();
            }
    
            // create one POA for default thread pool
            String specialPoaName = "POA-Default-Tester";
            POA specialPoa = rootPOA.create_POA(specialPoaName, null, null);
            specialPoa.activate_object(testerImpl);
            org.omg.CORBA.Object sref = 
                       specialPoa.servant_to_reference(testerImpl);
            Tester specialTesterRef = TesterHelper.narrow(sref);
            String sname = "DefaultTester";
            path = ncRef.to_name(sname);
            ncRef.rebind(path, specialTesterRef);
            specialPoa.the_POAManager().activate();

            U.sop(Options.defServerHandshake);

            orb.run();

        } catch (Throwable t) {
            U.sop("Unexpected throwable...");
            t.printStackTrace();
            System.exit(1);
        }
        U.sop("Ending successfully...");
    }
}
