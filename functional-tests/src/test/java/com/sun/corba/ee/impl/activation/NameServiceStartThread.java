/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.activation;


import java.io.File;

import org.omg.CosNaming.NamingContext;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.impl.naming.pcosnaming.NameService;
import com.sun.corba.ee.spi.misc.ORBConstants;

// REVISIT: After Merlin to see if we can get rid of this Thread and
// make the registration of PNameService for INS and BootStrap neat.
public class NameServiceStartThread extends java.lang.Thread
{
    private ORB orb;
    private File dbDir;         

    public NameServiceStartThread( ORB theOrb, File theDir ) 
    {
        orb = theOrb;
        dbDir = theDir;
    }

    public void run( )
    {
        try {
            // start Name Service
            NameService nameService = new NameService(orb, dbDir );
            NamingContext rootContext = nameService.initialNamingContext();
            orb.register_initial_reference( 
                ORBConstants.PERSISTENT_NAME_SERVICE_NAME, rootContext );
        } catch( Exception e ) {
            System.err.println( 
                "NameService did not start successfully" );
            e.printStackTrace( );
        }
    }
}
