/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.copyobjectpolicy;

import java.util.Map ;
import java.util.HashMap ;
import java.util.Hashtable ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Properties ;

import java.io.PrintStream ;
import java.io.DataInputStream ;

import java.rmi.RemoteException ;

import javax.rmi.PortableRemoteObject ;

import org.omg.CORBA.Policy ;
import org.omg.CORBA.ORB ;

import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantLocator ;
import org.omg.PortableServer.ServantManager ;
import org.omg.PortableServer.ServantRetentionPolicyValue ;
import org.omg.PortableServer.RequestProcessingPolicyValue ;
import org.omg.PortableServer.LifespanPolicyValue ;
import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.POAPackage.InvalidPolicy ;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists ;

import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.spi.extension.ServantCachingPolicy ;
import com.sun.corba.ee.spi.extension.CopyObjectPolicy ;

import corba.framework.TraceElement ;
import corba.framework.MethodEvent ;
import corba.framework.InternalProcess ;

public class Client implements InternalProcess
{
    public Client()
    {
    }

    private POA createPOA( POA rootPOA ) 
        throws AdapterAlreadyExists, InvalidPolicy,
            WrongPolicy, RemoteException
    {
        POA tpoa = rootPOA.create_POA( "POA1", rootPOA.the_POAManager(),
            new Policy[] {
                rootPOA.create_lifespan_policy(
                    LifespanPolicyValue.TRANSIENT),
                rootPOA.create_request_processing_policy(
                    RequestProcessingPolicyValue.USE_SERVANT_MANAGER),
                rootPOA.create_servant_retention_policy(
                    ServantRetentionPolicyValue.NON_RETAIN),
                ServantCachingPolicy.getFullPolicy() 
            } 
        ) ; 

        EchoImpl impl = new EchoImpl();
        Servant servant = (Servant)(javax.rmi.CORBA.Util.getTie( impl ) ) ;
        EchoServantLocator csl = new EchoServantLocator(servant);
        tpoa.set_servant_manager(csl);

        return tpoa ;
    }

    private POA createPOAWithCopyObjectPolicy( POA rootPOA ) 
        throws AdapterAlreadyExists, InvalidPolicy,
            WrongPolicy, RemoteException
    {
        POA tpoa = rootPOA.create_POA( "POA2", rootPOA.the_POAManager(), 
            new Policy[] {
                rootPOA.create_lifespan_policy(
                    LifespanPolicyValue.TRANSIENT),
                rootPOA.create_request_processing_policy(
                    RequestProcessingPolicyValue.USE_SERVANT_MANAGER),
                rootPOA.create_servant_retention_policy(
                    ServantRetentionPolicyValue.NON_RETAIN),
                ServantCachingPolicy.getFullPolicy(),
                new CopyObjectPolicy( UserConfigurator.REFERENCE_INDEX ) 
            } 
        ) ; 

        EchoImpl impl = new EchoImpl();
        Servant servant = (Servant)(javax.rmi.CORBA.Util.getTie( impl ) ) ;
        EchoServantLocator csl = new EchoServantLocator(servant);
        tpoa.set_servant_manager(csl);

        return tpoa ;
    }
        
    private Echo createEcho(POA tpoa)
    {
        // create an objref using POA
        byte[] id = "abcdef".getBytes();
        String intf = "" ; // new _EchoImpl_Tie()._all_interfaces(tpoa,id)[0];

        org.omg.CORBA.Object obj = tpoa.create_reference_with_id(id, intf);

        Echo echoRef 
            = (Echo)PortableRemoteObject.narrow(obj, Echo.class );

        return echoRef ; 
    }
    
    private void checkResult( String name )
    {
        // The expected result is that the make method is
        // entered, exited, entered, and exited, since
        // both the argument and the result are copier.
        MethodEvent mev = MethodEvent.make( name, 
            UserConfigurator.makeMethod ) ;
        List expected = new ArrayList() ;
        expected.add( new TraceElement( true, mev ) ) ;
        expected.add( new TraceElement( false, mev ) ) ;
        expected.add( new TraceElement( true, mev ) ) ;
        expected.add( new TraceElement( false, mev ) ) ;
        boolean ok = UserConfigurator.traceAccum.validate( expected ) ;
        if (!ok)
            throw new RuntimeException( "Test failed for " + name ) ;
    }

    private void performTest(PrintStream out, Echo echoRef,
        String interceptorName ) 
        throws RemoteException
    {
        UserConfigurator.traceAccum.clear() ;

        Map arg = new HashMap() ;
        Object result = echoRef.echo( arg ) ;

        checkResult( interceptorName ) ;
    }

    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        environment.list(out);

        try {
            // Create a new ORB with a user configurator to the ORB that uses 
            // ProxyInterceptors.
            environment.setProperty( "com.sun.corba.ee.ORBAllowLocalOptimization",
                "true" ) ;
            environment.setProperty( 
                "com.sun.corba.ee.ORBUserConfigurators.corba.copyobjectpolicy." +
                "UserConfigurator", "true" ) ;

            //environment.setProperty( "com.sun.corba.ee.ORBDebug", 
                //"transport,subcontract,poa,serviceContext,giop,giopVersion" ) ;

            ORB orb = ORB.init(args, environment);

            // Create two POAs, both with the minimal servant caching policy,
            //  one without a CopyObjectPolicy (test the default), and
            //  one with a non-default valued CopyObjectPolicy.
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();

            POA defaultPoa = createPOA(rootPOA);
            POA policyPoa = createPOAWithCopyObjectPolicy(rootPOA ) ;

            // Invoke on an objref created by each POA, using an operation that
            // will invoke copyObject.

            // Examine the traces and verify correct function.

            Echo echoRef1 = createEcho( defaultPoa ) ;
            performTest(out, echoRef1, UserConfigurator.VALUE_NAME );

            Echo echoRef2 = createEcho( policyPoa ) ;
            performTest(out, echoRef2, UserConfigurator.REFERENCE_NAME );
        } catch (Exception e) {
            e.printStackTrace(err);
            throw e;
        }
    }

    public static void main(String args[])
    {
        try {
            (new Client()).run(System.getProperties(),
                                      args,
                                      System.out,
                                      System.err,
                                      null);
        } catch (Exception e) {
            System.err.println("ERROR : " + e) ;
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}

class EchoServantLocator 
    extends org.omg.CORBA.LocalObject 
    implements ServantLocator
{
    Servant servant;

    EchoServantLocator(Servant servant)
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
