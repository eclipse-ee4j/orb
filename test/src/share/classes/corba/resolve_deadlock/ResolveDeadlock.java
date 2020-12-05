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

package corba.resolve_deadlock;

import java.util.Properties ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.naming.cosnaming.TransientNameService;

import org.omg.CORBA.ORB ;



public class ResolveDeadlock {   

    private static final String PORT_NUM = "3074" ;
    private static ORB serverORB ;

    private static void initializeORBs( String[] args ) {
        // The following must be set as system properties 
        System.setProperty( "javax.rmi.CORBA.PortableRemoteObjectClass",
            "com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject" ) ;
        System.setProperty( "javax.rmi.CORBA.StubClass",
            "com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl" ) ;
        System.setProperty( "javax.rmi.CORBA.UtilClass",
            "com.sun.corba.ee.impl.javax.rmi.CORBA.Util" ) ;

        // initializer server ORB.

        Properties serverProps = new Properties() ;
        serverProps.setProperty( "org.omg.CORBA.ORBSingletonClass",
            "com.sun.corba.ee.impl.orb.ORBSingleton" ) ;
        serverProps.setProperty( "org.omg.CORBA.ORBClass",
            "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
        serverProps.setProperty( ORBConstants.INITIAL_HOST_PROPERTY,
            "localhost" ) ;
        serverProps.setProperty( ORBConstants.INITIAL_PORT_PROPERTY,
            PORT_NUM ) ;
        serverProps.setProperty( ORBConstants.ALLOW_LOCAL_OPTIMIZATION,
            "true" ) ;  
        serverProps.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
            PORT_NUM ) ;
        serverProps.setProperty( ORBConstants.SERVER_HOST_PROPERTY,
            "localhost" ) ;
        serverProps.setProperty( ORBConstants.ORB_ID_PROPERTY,
            "serverORB" ) ;
        serverProps.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY,
            "300" ) ;

        // Ignore the args! Don't want to pick up setting of ORBInitialPort from args!
        String[] noArgs = null ;
        serverORB = ORB.init( noArgs, serverProps ) ;
        new TransientNameService( 
            com.sun.corba.ee.spi.orb.ORB.class.cast(serverORB) ) ;

        // Activate the transport
        try {
            serverORB.resolve_initial_references( "RootPOA" ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    public static void main( String[] args ) {
        initializeORBs( args ) ;
        try {
            //lookup a non-existing name "Foo"
            org.omg.CORBA.Object objRef = serverORB.resolve_initial_references( "Foo" );
            System.out.println( "Unexpectedly found the name Foo! ");
            System.exit(1);         
        } catch (Exception exc) {           
            System.out.println( "Expected exception in getting initial references: " + exc);
            exc.printStackTrace() ;
            System.exit(0) ;
        }
    }
}
