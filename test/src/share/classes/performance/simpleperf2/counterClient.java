/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package performance.simpleperf2;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;

import javax.rmi.PortableRemoteObject ;
import java.rmi.RemoteException ;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;

import corba.framework.InternalProcess;

public class counterClient implements InternalProcess
{
    private counterIF createLocalObject( ORB orb ) 
        throws java.rmi.RemoteException
    {
        counterImpl cimpl = new counterImpl() ;
         
        return cimpl ;
    }

    private counterIF createRemoteObject( ORB orb ) 
        throws java.rmi.RemoteException
    {
        counterImpl obj = new counterImpl() ;

        counterIF counterRef 
            = (counterIF)PortableRemoteObject.narrow(obj, counterIF.class );

        return counterRef ;
    }

    private counterIF createRemoteObjectMarshal( ORB orb )
        throws java.rmi.RemoteException, java.rmi.NoSuchObjectException
    {
        counterImpl obj = new counterImpl() ;

        counterIF counterRef 
            = (counterIF)PortableRemoteObject.narrow(obj, counterIF.class );

        java.rmi.Remote stub = PortableRemoteObject.toStub( counterRef ) ;
        
        String str = orb.object_to_string( (org.omg.CORBA.Object)stub ) ;
        org.omg.CORBA.Object obj2 = orb.string_to_object( str ) ;

        return (counterIF)(PortableRemoteObject.narrow( obj2,
            counterIF.class )) ;
    }

    private static final int COUNT = 10000 ;

    private void performTest(PrintStream out, counterIF counterRef, 
        String testType ) throws RemoteException
    {
        long time = System.currentTimeMillis() ;
        long value = 0 ;

        for (int i = 0; i < COUNT; i++) {
            value += counterRef.increment(1);
        }
        
        double elapsed = System.currentTimeMillis() - time ;

        out.println( "Test " + testType + ": Elapsed time per invocation = " + 
            elapsed/COUNT + " milliseconds" ) ;
    }

    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        environment.list(out);

        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, environment);

            counterIF counterRef1 = createLocalObject( orb ) ;
            performTest(out, counterRef1, "local object" );

            counterIF counterRef2 = createRemoteObject( orb ) ;
            performTest(out, counterRef2, "local RMI-IIOP" );
/* There are problems here that need further investigation
            counterIF counterRef3 = createRemoteObjectMarshal( orb ) ;
            performTest(out, counterRef3, "local RMI-IIOP (marshalled)" );
*/
        } catch (Exception e) {
            e.printStackTrace(err);
            throw e;
        }
    }

    public static void main(String args[])
    {
        try {
            (new counterClient()).run(System.getProperties(),
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

class CounterServantLocator extends org.omg.CORBA.LocalObject implements ServantLocator
{
    Servant servant;

    CounterServantLocator(Servant servant)
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
