/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.naming.pcosnaming;

import java.io.File;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.CorbaResourceUtil;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.activation.InitialNameService;
import com.sun.corba.ee.spi.activation.InitialNameServiceHelper;
import org.omg.CosNaming.NamingContext;
/**
 * Class NameServer is a standalone application which
 * implements a persistent and a transient name service.
 * It uses the PersistentNameService and TransientNameService
 * classes for the name service implementation.
 *
 * @version     1.1, 99/10/07
 * @author      Hemanth Puttaswamy
 * @since       JDK1.2
 */

public class NameServer 
{
    private ORB orb;

    private File dbDir; // name server database directory

    private final static String dbName = "names.db";

    public static void main(String args[]) 
    {
        NameServer ns = new NameServer(args);
        ns.run();
    }

    protected NameServer(String args[]) 
    {
        // create the ORB Object
        java.util.Properties props = System.getProperties();
        props.put( ORBConstants.ORB_SERVER_ID_PROPERTY, "1000" ) ;
        props.put("org.omg.CORBA.ORBClass", 
                  "com.sun.corba.ee.impl.orb.ORBImpl");
        orb = (ORB) org.omg.CORBA.ORB.init(args,props);

        // set up the database directory
        String dbDirName = props.getProperty( ORBConstants.DB_DIR_PROPERTY ) +
            props.getProperty("file.separator") + dbName + 
            props.getProperty("file.separator");

        dbDir = new File(dbDirName);
        if (!dbDir.exists()) {
            boolean result = dbDir.mkdir();
            if (!result) {
                throw new RuntimeException( "Could not create directory "
                    + dbDirName ) ;
            }
        }
    }

    protected void run() 
    {
        try {

            // create the persistent name service
            NameService ns = new NameService(orb, dbDir);

            // add root naming context to initial naming
            NamingContext rootContext = ns.initialNamingContext();
            InitialNameService ins = InitialNameServiceHelper.narrow(
                                     orb.resolve_initial_references(
                                     ORBConstants.INITIAL_NAME_SERVICE_NAME ));
            ins.bind( "NameService", rootContext, true);
            System.out.println(CorbaResourceUtil.getText("pnameserv.success"));

            // wait for invocations
            orb.run();

        } catch (Exception ex) {

            ex.printStackTrace(System.err);
        }
    }

}
