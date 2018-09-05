/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package naming.rinameservice;

import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import corba.framework.*;
import java.util.*;
import java.io.*;
import com.sun.corba.ee.spi.misc.ORBConstants ;

/**
 * NameServiceClient just tests that StandAlone Name Service that we
 * ship with RI. The test makes sure that 
 * 1. Root NamingContext can be resolved using the -ORBInitRef property
 * 2. Basic operations of Bind and Resolve works fine 
 */
public class NameServiceClient implements InternalProcess {
    public static void main( String args[] ) {
        try {
            (new NameServiceClient()).run( System.getProperties(),
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
            System.out.println( "Start of NameService test" ) ;

            String orbArguments[] = new String[2];
            orbArguments[0] = "-ORBInitRef";
            orbArguments[1] = "NameService=corbaloc:iiop:1.2@localhost:" +
                TestConstants.RI_NAMESERVICE_PORT + "/NameService";
            Properties props = new Properties() ;
            props.setProperty( ORBConstants.DEBUG_PROPERTY, "subcontract,giop,transport" ) ;
            ORB orb = ORB.init( orbArguments, props );
            System.out.println( "Created ORB" ) ;

            org.omg.CORBA.Object object = orb.resolve_initial_references(
                "NameService" );
            System.out.println( "Resolved NameService" ) ;
            
            org.omg.CosNaming.NamingContextExt namingContext =
                 org.omg.CosNaming.NamingContextExtHelper.narrow( object );
            System.out.println( "Narrowed NameService" ) ;

            NameComponent[] name = new NameComponent[1];
            name[0] = new NameComponent();
            name[0].id = "Test";
            name[0].kind = "";
            namingContext.bind( name, object );
            System.out.println( "Bound object in NameService" ) ;

            object = namingContext.resolve_str( "Test" );
            if( object == null ) {
                System.err.println( "NamingContext resolve failed..." );
                System.exit( 1 );
            }
            System.out.println( "Successfully resolved Standalone Name Server" +
                " Using INS" );
        } catch ( Exception e ) {
            System.out.println( "Caught exception " + e ) ;
            e.printStackTrace();
            System.exit( 1 );
        }
    }
}


