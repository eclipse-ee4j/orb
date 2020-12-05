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

package com.sun.corba.ee.internal.CosNaming;

import java.util.Properties;

import java.io.File;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.resolver.Resolver ;
import com.sun.corba.ee.spi.resolver.LocalResolver ;
import com.sun.corba.ee.spi.resolver.ResolverDefault ;

import com.sun.corba.ee.spi.misc.ORBConstants;

/**
 * Class BootstrapServer is the main entry point for the bootstrap server
 * implementation.  The BootstrapServer makes all object references
 * defined in a configurable file available using the old
 * naming bootstrap protocol.
 */
public class BootstrapServer
{
    private ORB orb;

     /**
     * Main startup routine for the bootstrap server.
     * It first determines the port on which to listen, checks that the
     * specified file is available, and then creates the resolver 
     * that will be used to service the requests in the 
     * BootstrapServerRequestDispatcher.
     * @param args the command-line arguments to the main program.
     */
    public static final void main(String[] args)
    {
        String propertiesFilename = null;
        int initialPort = ORBConstants.DEFAULT_INITIAL_PORT;

        // Process arguments
        for (int i=0;i<args.length;i++) {
            // Look for the filename
            if (args[i].equals("-InitialServicesFile") && i < args.length -1) {
                propertiesFilename = args[i+1];
            }

            // Was the initial port specified? If so, override
            // This property normally is applied for the client side
            // configuration of resolvers.  Here we are using it to
            // define the server port that the with which the resolvers
            // communicate.
            if (args[i].equals("-ORBInitialPort") && i < args.length-1) {
                initialPort = java.lang.Integer.parseInt(args[i+1]);
            }
        }

        if (propertiesFilename == null) {
            System.out.println( 
                "Bootstrapserver -InitialServicesFile <filename> "
                    + "-ORBInitialPort <num>" ) ;
            return;
        }

        // Create a file
        File file = new File(propertiesFilename);

        // Verify that if it exists, it is readable
        if (file.exists() == true && file.canRead() == false) {
            System.err.println( "File " + file.getAbsolutePath() 
                + " is not readable" ) ;
            return;
        }

        // Success: start up
        System.out.println(
            "Bootstrapserver started on port " + Integer.toString(initialPort) 
                + " wirh InitialServicesFile " + file.getAbsolutePath());

        Properties props = new Properties() ;

        // Use the SERVER_PORT to create an Acceptor using the
        // old legacy code in ORBConfiguratorImpl.  When (if?)
        // the legacy support is removed, this code will need
        // to create an Acceptor directly.
        props.put( ORBConstants.SERVER_PORT_PROPERTY,  
            Integer.toString( initialPort ) ) ;

        ORB orb = (ORB) org.omg.CORBA.ORB.init(args,props);

        LocalResolver lres = orb.getLocalResolver() ;
        Resolver fres = ResolverDefault.makeFileResolver( orb, file ) ;
        Resolver cres = ResolverDefault.makeCompositeResolver( fres, lres ) ;
        LocalResolver sres = ResolverDefault.makeSplitLocalResolver( cres, lres ) ;

        orb.setLocalResolver( sres ) ;

        try {
            // This causes the acceptors to start listening.
            orb.resolve_initial_references(ORBConstants.ROOT_POA_NAME);
        } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
            throw new RuntimeException("This should not happen", e);
        }

        orb.run() ;
    }
}
