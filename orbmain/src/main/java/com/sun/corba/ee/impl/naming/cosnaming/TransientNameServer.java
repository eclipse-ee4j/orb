/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.naming.cosnaming;

import java.util.Properties;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.CorbaResourceUtil;
import com.sun.corba.ee.spi.logging.NamingSystemException;
import com.sun.corba.ee.spi.trace.Naming;

/**
 * Class TransientNameServer is a standalone application which
 * implements a transient name service. It uses the TransientNameService
 * class for the name service implementation, and the BootstrapServer
 * for implementing bootstrapping, i.e., to get the initial NamingContext.
 * <p>
 * The BootstrapServer uses a Properties object specify the initial service
 * object references supported; such as Properties object is created containing
 * only a "NameService" entry together with the stringified object reference
 * for the initial NamingContext. The BootstrapServer's listening port
 * is set by first checking the supplied arguments to the name server
 * (-ORBInitialPort), and if not set, defaults to the standard port number.
 * The BootstrapServer is created supplying the Properties object, using no
 * external File object for storage, and the derived initial port number.
 * @see TransientNameService
 * @see BootstrapServer
 */
@Naming
public class TransientNameServer
{
    static private boolean debug = false ;
    private static final NamingSystemException wrapper =
        NamingSystemException.self ;

    @Naming
    private static org.omg.CORBA.Object initializeRootNamingContext( ORB orb ) {
        org.omg.CORBA.Object rootContext = null;
        try {
            com.sun.corba.ee.spi.orb.ORB coreORB =
                (com.sun.corba.ee.spi.orb.ORB)orb ; 
                
            TransientNameService tns = new TransientNameService(coreORB );
            return tns.initialNamingContext();
        } catch (org.omg.CORBA.SystemException e) {
            throw wrapper.transNsCannotCreateInitialNcSys( e ) ;
        } catch (Exception e) {
            throw wrapper.transNsCannotCreateInitialNc( e ) ;
        }
    }

    /**
     * Main startup routine. It instantiates a TransientNameService
     * object and a BootstrapServer object, and then allows invocations to
     * happen.
     * @param args an array of strings representing the startup arguments.
     */
    @Naming
    public static void main(String args[]) {
        boolean invalidHostOption = false;
        boolean orbInitialPort0 = false;

        // Determine the initial bootstrap port to use
        int initialPort = 0;
        try {
            // Create an ORB object
            Properties props = System.getProperties() ;

            props.put( ORBConstants.ORB_SERVER_ID_PROPERTY,
                ORBConstants.NAME_SERVICE_SERVER_ID ) ;
            props.put( "org.omg.CORBA.ORBClass", 
                "com.sun.corba.ee.impl.orb.ORBImpl" );

            String ips = null ;
            try {
                ips = System.getProperty( ORBConstants.INITIAL_PORT_PROPERTY ) ;
                if (ips != null && ips.length() > 0 ) {
                    initialPort = java.lang.Integer.parseInt(ips);
                    if( initialPort == 0 ) {
                        orbInitialPort0 = true;
                        throw wrapper.transientNameServerBadPort() ;
                    }
                }

                String hostName = 
                    System.getProperty( ORBConstants.INITIAL_HOST_PROPERTY ) ;

                if( hostName != null ) {
                    invalidHostOption = true;
                    throw wrapper.transientNameServerBadHost() ;
                }
            } catch (java.lang.NumberFormatException e) {
                wrapper.badInitialPortValue( ips, e ) ;
            }

            // Let arguments override
            for (int i=0;i<args.length;i++) {
                if (args[i].equals("-ORBInitialPort") &&
                    i < args.length-1) {
                    initialPort = java.lang.Integer.parseInt(args[i+1]);

                    if( initialPort == 0 ) {
                        orbInitialPort0 = true;
                        throw wrapper.transientNameServerBadPort() ;
                    }
                }

                if (args[i].equals("-ORBInitialHost" ) ) { 
                    invalidHostOption = true;
                    throw wrapper.transientNameServerBadHost() ;
                }
            }

            // If initialPort is not set, then we need to set the Default 
            // Initial Port Property for the ORB
            if( initialPort == 0 ) {
                initialPort = ORBConstants.DEFAULT_INITIAL_PORT;
                props.put( ORBConstants.INITIAL_PORT_PROPERTY,
                    java.lang.Integer.toString(initialPort) );
            }

            // Set -ORBInitialPort = Persistent Server Port so that ORBImpl
            // will start Boot Strap.
            props.put( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, 
               java.lang.Integer.toString(initialPort) );

            org.omg.CORBA.ORB corb = ORB.init( args, props ) ;
  
            org.omg.CORBA.Object ns = initializeRootNamingContext( corb ) ;
            ((com.sun.corba.ee.org.omg.CORBA.ORB)corb).register_initial_reference( 
                "NamingService", ns ) ;

            String stringifiedIOR = null;
 
            if( ns != null ) {
                stringifiedIOR = corb.object_to_string(ns) ;
            } else {
                 NamingUtils.errprint(CorbaResourceUtil.getText(
                     "tnameserv.exception", initialPort));
                 NamingUtils.errprint(CorbaResourceUtil.getText(
                     "tnameserv.usage"));
                System.exit( 1 );
            }

            // This is used for handshaking by the IBM test framework!
            // Do not modify, unless another synchronization protocol is 
            // used to replace this hack!

            System.out.println(CorbaResourceUtil.getText(
                "tnameserv.hs1", stringifiedIOR));
            System.out.println(CorbaResourceUtil.getText(
                "tnameserv.hs2", initialPort));
            System.out.println(CorbaResourceUtil.getText("tnameserv.hs3"));

            // Serve objects.
            java.lang.Object sync = new java.lang.Object();
            synchronized (sync) {sync.wait();}
        } catch (Exception e) {
            if( invalidHostOption ) {
                // Let the User Know that -ORBInitialHost is not valid for
                // tnameserver
                NamingUtils.errprint( CorbaResourceUtil.getText(
                    "tnameserv.invalidhostoption" ) );
            } else if( orbInitialPort0 ) {
                // Let the User Know that -ORBInitialPort 0 is not valid for
                // tnameserver
                NamingUtils.errprint( CorbaResourceUtil.getText(
                    "tnameserv.orbinitialport0" ));
            } else {
                NamingUtils.errprint(CorbaResourceUtil.getText(
                    "tnameserv.exception", initialPort));
                NamingUtils.errprint(CorbaResourceUtil.getText(
                    "tnameserv.usage"));
            }
        }
    }

    /**
     * Private constructor since no object of this type should be instantiated.
     */ 
    private TransientNameServer() {}
}
