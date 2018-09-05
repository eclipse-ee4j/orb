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

public class Client {
    private static org.omg.CORBA.ORB orb;

    public static void main( String[] args ) { 
        try {
            orb = org.omg.CORBA.ORB.init( args, null );

            org.omg.CORBA.Object obj = 
                orb.resolve_initial_references( "NameService");
            org.omg.CosNaming.NamingContextExt nctx = 
                org.omg.CosNaming.NamingContextExtHelper.narrow( obj );

            obj = nctx.resolve_str( "idlI1" );

            System.out.println( "Before Calling i1.narrow() ");
            System.out.flush();
            idlI1 i1 = idlI1Helper.narrow( obj );

            System.out.println( "invoking i1.o1() ");
            System.out.flush();
            i1.o1( "Involing from the Client..." );
            System.exit( 0 ) ;
        } catch( Exception e ) {
            e.printStackTrace( );
            System.exit(1) ;
        }
    }
}
