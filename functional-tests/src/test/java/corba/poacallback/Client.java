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
