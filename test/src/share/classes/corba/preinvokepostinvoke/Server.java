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

package corba.preinvokepostinvoke;

import org.omg.CORBA.Policy;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.ServantLocator;
import javax.rmi.PortableRemoteObject ;
import com.sun.corba.ee.spi.extension.ServantCachingPolicy;

public class Server {

     private static ORB orb;
     private static  org.omg.CosNaming.NamingContextExt nctx; 
     private static  POA poaWithServantCachingPolicy; 


     public static void main( String[] args ) {
        System.out.println( " Starting Server.... " );
        System.out.flush( );
     
        try {
            orb = ORB.init( args, null );

            org.omg.CORBA.Object obj = 
                orb.resolve_initial_references( "NameService");
            nctx = org.omg.CosNaming.NamingContextExtHelper.narrow( obj );

            POA rPOA = (POA)orb.resolve_initial_references( "RootPOA" );
            rPOA.the_POAManager().activate( );

            Policy[] policies = new Policy[3];
            policies[0] = rPOA.create_servant_retention_policy(
                ServantRetentionPolicyValue.NON_RETAIN);
            policies[1] = rPOA.create_request_processing_policy(
                          RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
            policies[2] = ServantCachingPolicy.getFullPolicy( );

            MyServantLocator sl = new MyServantLocator( orb );

            poaWithServantCachingPolicy = rPOA.create_POA( "poa", null, 
                policies );
            poaWithServantCachingPolicy.set_servant_manager( sl );
            poaWithServantCachingPolicy.the_POAManager().activate();


            _Interface_Stub s = new _Interface_Stub( );
            bindInstance( (s._ids())[0], "Instance1" );
            System.out.println( "Created and Bound instance1" );
            System.out.flush( );

            bindInstance( (s._ids())[0], "Instance2" );
            System.out.println( "Created and Bound instance2" );
            System.out.flush( );

            TestAssert.startTest( );
            resolveReferenceAndInvoke( orb  );
            TestAssert.isTheCallBalanced( 2 );

            // Emit the handshake the test framework expects
            // (can be changed in Options by the running test)
            System.out.println ("Server is ready.");
            System.out.flush( );
        } catch( Exception e ) {
            e.printStackTrace( );
        }
    }

    private static void bindInstance( String repId, String bindingName )
    {
        try {
            org.omg.CORBA.Object obj = 
                poaWithServantCachingPolicy.create_reference_with_id(
                    bindingName.getBytes( ), repId );
            org.omg.CosNaming.NameComponent[] nc = nctx.to_name( bindingName );
            nctx.rebind( nc, obj );
        } catch( Exception e ) {
            e.printStackTrace( );
        }
    }

    private static void resolveReferenceAndInvoke(ORB orb) {
        try {
             org.omg.CORBA.Object obj;

             obj = nctx.resolve_str( "Instance1" );
             Interface i1 = 
                 (Interface) PortableRemoteObject.narrow(obj,Interface.class );
             i1.o1( "Invoking from Client..." );
        }catch( Exception e ) {
            e.printStackTrace( );
            System.exit( 1 );
        }
    }
}

        
        


       
    



