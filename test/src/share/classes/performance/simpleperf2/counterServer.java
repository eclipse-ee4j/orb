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

package performance.simpleperf2;

import javax.rmi.PortableRemoteObject ;
import java.io.*;
import java.io.DataOutputStream ;
import java.util.*;
import java.rmi.RemoteException ;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.ServantLocatorPackage.*;
import com.sun.corba.ee.spi.misc.ORBConstants ;
import corba.framework.ThreadProcess ;

public class counterServer extends ThreadProcess {

    public void run()
    {
        try{
            // create and initialize the ORB
            Properties p = new Properties();
            p.put("org.omg.CORBA.ORBClass",  
                "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            p.put( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, "9999");
            p.put( ORBConstants.ORB_SERVER_ID_PROPERTY, "9999");
            String[] args = null ;
            ORB orb = ORB.init(args, p);

            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();

            POA poa = createPOA(orb, rootPOA);
            createCounter1(orb, poa);

            // wait for invocations from clients
            System.out.println("Server is ready.");
            orb.run();

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private POA createPOA(ORB orb, POA rootPOA)
        throws Exception
    {
        // create a persistent POA
        Policy[] tpolicy = new Policy[3];
        tpolicy[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);
        tpolicy[1] = rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
        tpolicy[2] = rootPOA.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN) ;
        POA tpoa = rootPOA.create_POA("PersistentPOA", null, tpolicy);
 
        counterImpl impl = new counterImpl();
        Servant servant = (Servant)(javax.rmi.CORBA.Util.getTie( impl ) ) ;
        CSLocator csl = new CSLocator(servant);
        tpoa.set_servant_manager(csl);
        tpoa.the_POAManager().activate();
        return tpoa;
    }

    private void createCounter1(ORB orb, POA tpoa)
        throws Exception
    {
        // create an objref using POA
        byte[] id = "abcdef".getBytes();
        String intf = "" ; // new _counterImpl_Tie()._all_interfaces(tpoa,id)[0];

        org.omg.CORBA.Object obj = tpoa.create_reference_with_id(id, intf);

        counterIF counterRef 
            = (counterIF)PortableRemoteObject.narrow(obj, counterIF.class );

        // put objref in NameService
        org.omg.CORBA.Object objRef =
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        NameComponent nc = new NameComponent("Counter1", "");
        NameComponent path[] = {nc};

        ncRef.rebind(path, obj);
    }
}

class CSLocator extends org.omg.CORBA.LocalObject implements ServantLocator
{
    Servant servant;

    CSLocator(Servant servant)
    {
        this.servant = servant;
    }

    public Servant preinvoke(byte[] oid, POA adapter, String operation, 
                             CookieHolder the_cookie)
        throws org.omg.PortableServer.ForwardRequest
    {
        return servant ;
    }

    public void postinvoke(byte[] oid, POA adapter, String operation, 
                           java.lang.Object cookie, Servant servant)
    {
        return;
    }
}
