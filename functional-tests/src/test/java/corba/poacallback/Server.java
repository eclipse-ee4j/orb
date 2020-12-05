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

        
        


       
    



