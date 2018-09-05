/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package naming.instest;

import java.util.Properties;
import java.util.Hashtable;
import com.sun.corba.ee.spi.misc.ORBConstants;
import java.io.PrintStream;
import org.omg.CORBA.ORB;

public class INSServer
{

    public static void main(String args[]) {
        try {
            (new INSServer()).run( System.getProperties(),
                                args, System.out, System.err, null );
        } catch( Exception e ) {
            e.printStackTrace( System.err );
            System.exit( 1 );
        }
    }


    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        try {

            System.out.println("ORB class: "
                               + environment.getProperty("org.omg.CORBA.ORBClass"));

            // environment.setProperty( "com.sun.corba.ee.ORBDebug", 
                // "subcontract,transport,naming,serviceContext,transientObjectManager" ) ;
            ORB orb = ORB.init(args, environment);

            HelloImpl helloRef = new HelloImpl( );
            orb.connect( helloRef );
            ((com.sun.corba.ee.spi.orb.ORB)orb).register_initial_reference( 
                TestConstants.INSServiceName, helloRef );

            //handshake:
            out.println("Server is ready.");
            out.flush();

            orb.run( );
        } catch( Exception e ) {
            e.printStackTrace( System.err );
            System.exit( 1 );
        }
    }
}

        
        



