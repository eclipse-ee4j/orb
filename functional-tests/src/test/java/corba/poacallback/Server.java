/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poacallback;

import org.omg.CORBA.Policy;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.ServantLocator;

public class Server {

     private static ORB orb;


     public static void main( String[] args ) {
        System.out.println( " Starting Server.... " );
        System.out.flush( );
     
        try {
            orb = ORB.init( args, null );
            createReferenceAndBind( "poa1", new idlI1ServantLocator( orb ),
                idlI1Helper.id( ), "idlI1" );
            System.out.println( "Created and Bound idlI1" );
            System.out.flush( );
            createReferenceAndBind( "poa2", new idlI2ServantLocator( orb ),
                idlI2Helper.id( ), "idlI2" );
            System.out.println( "Created and Bound idlI2" );
            System.out.flush( );

            // Emit the handshake the test framework expects
            // (can be changed in Options by the running test)
            System.out.println ("Server is ready.");
            System.out.flush( );

            orb.run( );
        } catch( Exception e ) {
            e.printStackTrace( );
        }
    }

    private static void createReferenceAndBind( String poaName, 
        ServantLocator locator, String repId, String bindingName )
    {
        try {
            POA rPOA = (POA)orb.resolve_initial_references( "RootPOA" );
            rPOA.the_POAManager().activate( );

            Policy[] policies = new Policy[2];
            policies[0] = rPOA.create_servant_retention_policy(
                ServantRetentionPolicyValue.NON_RETAIN);
            policies[1] = rPOA.create_request_processing_policy(
                          RequestProcessingPolicyValue.USE_SERVANT_MANAGER);

            org.omg.CORBA.Object obj = 
                orb.resolve_initial_references( "NameService");
            org.omg.CosNaming.NamingContextExt nctx = 
                org.omg.CosNaming.NamingContextExtHelper.narrow( obj );

            POA poa = rPOA.create_POA( poaName, null, policies );
            poa.set_servant_manager( locator );
            obj = poa.create_reference_with_id( 
                (new String( "idlI")).getBytes( ), repId );

            poa.the_POAManager().activate();


            org.omg.CosNaming.NameComponent[] nc = 
                nctx.to_name( bindingName );
            nctx.rebind( nc, obj );
        } catch( Exception e ) {
            e.printStackTrace( );
        }
    }
}

        
        


       
    



