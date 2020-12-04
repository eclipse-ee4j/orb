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


