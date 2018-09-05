/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package performance.simpleperf;

import javax.rmi.PortableRemoteObject ;
import java.util.Properties ;

import org.omg.CORBA.ORB ;
import org.omg.CORBA.Policy ;
import org.omg.CosNaming.NameComponent ;
import org.omg.CosNaming.NamingContext ;
import org.omg.CosNaming.NamingContextHelper ;
import org.omg.PortableServer.LifespanPolicyValue ;
import org.omg.PortableServer.POA ;
import org.omg.PortableServer.RequestProcessingPolicyValue ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantLocator ;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;
import org.omg.PortableServer.ServantRetentionPolicyValue ;

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
