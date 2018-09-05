/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.driinvocation;

import javax.rmi.PortableRemoteObject ;
import java.rmi.RemoteException ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.misc.ORBConstants ;
import corba.adapteractivator.AdapterActivator;
import corba.driinvocation.Echo ;
import corba.driinvocation.EchoHelper ;
import java.util.Properties;
import javax.rmi.CORBA.Tie;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;
import org.omg.PortableServer.ServantRetentionPolicyValue;

public class Server {
    public static boolean debug = true;
    public static String Echo1Id = "abcdef";
    public static String Echo2driinvocation = "qwerty";

    public static void main(String args[])
    {
        try{
            // set debug flag
            if ( args.length > 0 && args[0].equals("-debug") )
                debug = true;

            if (debug) {
                System.out.println("ENTER: Server");
                System.out.flush();
            }

            // create and initialize the ORB
            Properties p = new Properties();
            p.put("org.omg.CORBA.ORBClass", 
                  System.getProperty("org.omg.CORBA.ORBClass"));
            p.put( ORBConstants.ORB_SERVER_ID_PROPERTY, "9999");
            ORB orb = (ORB) ORB.init(args, p);

            if (debug) {
                System.out.println("Server: ORB initialized");
                System.out.flush();
            }

            // get rootPOA, set the AdapterActivator, and activate RootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_activator(new MyAdapterActivator(orb));
            rootPOA.the_POAManager().activate();

            if (debug) {
                System.out.println("Server: RootPOA activator set");
                System.out.flush();
            }

            POA poa = createPersistentPOA(orb, rootPOA);
            createEcho1(orb, poa);
            poa = createNonRetainPOA(orb, rootPOA);
            createEcho2(orb, poa);
            if (debug) {
                System.out.println("Server: refs created");
                System.out.flush();
            }

            // wait for invocations from clients
            System.out.println("Server is ready.");
            System.out.flush();

            orb.run();

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
            System.exit(1);
        } finally {
            if (debug) {
                System.out.println("EXIT: Server");
                System.out.flush();
            }
        }
    }

    static POA createPersistentPOA(ORB orb, POA rootPOA)
        throws Exception
    {
        // create a persistent POA
        Policy[] tpolicy = new Policy[2];
        tpolicy[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
        tpolicy[1] = rootPOA.create_request_processing_policy(
            RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
        POA tpoa = rootPOA.create_POA("PersistentPOA", null, tpolicy);
 
        // register the ServantActivator with the POA, then activate POA
        EchoServantActivator csa = new EchoServantActivator(orb);
        tpoa.set_servant_manager(csa);
        tpoa.the_POAManager().activate();
        return tpoa;
    }

    static Tie makeEchoServant( ORB orb ) 
    {
        EchoImpl impl = null ;

        try {
            impl = new EchoImpl(orb, Server.debug);
        } catch (RemoteException exc) {
            // ignore
        }

        Tie tie = ORB.getPresentationManager().getTie() ;
        tie.setTarget( impl ) ;

        return tie ;
    }

    static void createEcho1(ORB orb, POA tpoa)
        throws Exception
    {
        // create an objref using persistent POA
        byte[] id = Echo1Id.getBytes();
        String intf = makeEchoServant(orb)._all_interfaces(tpoa,id)[0] ; 

        org.omg.CORBA.Object obj = tpoa.create_reference_with_id(id, intf);

        Class intfr = Class.forName("corba.driinvocation.Echo");

        Echo echoRef 
            = (Echo)PortableRemoteObject.narrow(obj, Echo.class );

        // put objref in NameService
        org.omg.CORBA.Object objRef =
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        NameComponent nc = new NameComponent("Echo1", "");
        NameComponent path[] = {nc};

        ncRef.rebind(path, obj);

        // invoke on the local objref to test local invocations
        if ( Server.debug ) 
            System.out.println("\nTesting local invocation: Client thread is "
                +Thread.currentThread());
        int value = echoRef.double(1);
        if ( Server.debug ) 
            System.out.println(value);
    }

    static POA createNonRetainPOA(ORB orb, POA rootPOA)
        throws Exception
    {
        // create another persistent, non-retaining POA
        Policy[] tpolicy = new Policy[3];
        tpolicy[0] = rootPOA.create_lifespan_policy(
            LifespanPolicyValue.PERSISTENT);
        tpolicy[1] = rootPOA.create_request_processing_policy(
            RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
        tpolicy[2] = rootPOA.create_servant_retention_policy(
            ServantRetentionPolicyValue.NON_RETAIN);
        POA tpoa = rootPOA.create_POA("NonRetainPOA", null, tpolicy);
        
        // register the ServantLocator with the POA, then activate POA
        EchoServantLocator csl = new EchoServantLocator(orb);
        tpoa.set_servant_manager(csl);
        tpoa.the_POAManager().activate();
        return tpoa;
    }

    static void createEcho2(ORB orb, POA tpoa)
        throws Exception
    {
        // create a servant and get an objref using persistent POA
        byte[] id = Echo2Id.getBytes();
        String intf = makeEchoServant(orb)._all_interfaces(tpoa,id)[0] ; 
        org.omg.CORBA.Object obj = tpoa.create_reference_with_id(id, intf ) ; 
        Echo echoRef = (Echo)PortableRemoteObject.narrow(obj, Echo.class );

        // put objref in NameService
        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        NameComponent nc = new NameComponent("Echo2", "");
        NameComponent path[] = {nc};
        ncRef.rebind(path, echoRef);
    }
}


class MyAdapterActivator extends LocalObject implements AdapterActivator
{
    private ORB orb;

    MyAdapterActivator(ORB orb)
    {
        this.orb = orb;
    }

    public boolean unknown_adapter(POA parent, String name)
    {
        if ( Server.debug ) 
            System.out.println("\nIn MyAdapterActivator.unknown_adapter, parent = " +
                parent.the_name()+" child = "+name);

        try {
            if ( name.equals("PersistentPOA") )
                Server.createPersistentPOA(orb, parent);
            else if ( name.equals("NonRetainPOA") )
                Server.createNonRetainPOA(orb, parent);
            else 
                return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }
}


class EchoServantActivator extends org.omg.CORBA.LocalObject implements ServantActivator
{
    ORB orb;

    EchoServantActivator(ORB orb)
    {
        this.orb = orb;
    }

    public Servant incarnate(byte[] oid, POA adapter)
        throws org.omg.PortableServer.ForwardRequest
    {
        Servant servant = Server.makeEchoServant( orb ) ;

        if ( Server.debug ) 
            System.out.println("\nIn EchoServantActivator.incarnate,   oid = "
                               +oid
                               +" poa = "+adapter.the_name()
                               +" servant = "+servant);
        return servant;
    }

    public void etherealize(byte[] oid, POA adapter, Servant servant, 
                            boolean cleanup_in_progress, boolean remaining_activations)
    {
        if ( Server.debug ) 
            System.out.println("\nIn EchoServantActivator.etherealize, oid = "
                               +oid
                               +" poa = "+adapter.the_name()
                               +" servant = "+servant
                               +" cleanup_in_progress = "+cleanup_in_progress
                               +" remaining_activations = "+remaining_activations);
        return;
    }
}

class EchoServantLocator extends org.omg.CORBA.LocalObject implements ServantLocator
{
    ORB orb;

    EchoServantLocator(ORB orb)
    {
        this.orb = orb;
    }

    public Servant preinvoke(byte[] oid, POA adapter, String operation, 
                             CookieHolder the_cookie)
        throws org.omg.PortableServer.ForwardRequest
    {
        String sid = new String(oid);
        String newidStr = "somethingdifferent";

        // Tests location forwards
        if ( sid.equals(Server.Echo2Id) ) { 
            // construct a new objref to forward to.
            byte[] id = newidStr.getBytes();
            org.omg.CORBA.Object obj = null;
            try {
                String intf = makeEchoServant(orb)._all_interfaces(tpoa,id)[0] ; 
                obj = adapter.create_reference_with_id(id, intf ) ;
            } catch ( Exception ex ) {}
            Echo echoRef = (Echo)PortableRemoteObject.narrow(obj, Echo.class );

            System.out.println("\nEchoServantLocator.preinvoke forwarding ! "
                               +"old oid ="+new String(oid)
                               +"new id ="+new String(id));

            ForwardRequest fr = new ForwardRequest(obj);
            throw fr;
        }

        String oidStr = new String(oid);
        if ( !newidStr.equals(oidStr) )
            System.err.println("\tERROR !!!: preinvoke got wrong id:"+oidStr);

        MyCookie cookie = new MyCookie();
        Servant servant = Server.makeEchoServant( orb ) ;

        if ( Server.debug ) 
            System.out.println("\nIn EchoServantLocator.preinvoke,  oid = "
                               +oidStr
                               +" poa = "+adapter.the_name()
                               +" operation = " +operation
                               +" cookie = "+cookie+" servant = "+servant);

        the_cookie.value = cookie;
        return servant;
    }

    public void postinvoke(byte[] oid, POA adapter, String operation, 
                           java.lang.Object cookie, Servant servant)
    {
        if ( Server.debug ) 
            System.out.println("\nIn EchoServantLocator.postinvoke, oid = "
                               +new String(oid)
                               +" poa = "+adapter.the_name()
                               +" operation = " +operation
                               +" cookie = "+cookie+" servant = "+servant);
        return;
    }
}

class MyCookie 
{}
