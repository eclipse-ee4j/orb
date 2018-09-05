/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2003 Dec 11 (Thu) 11:03:27 by Harold Carr.
// Last Modified : 2003 Dec 18 (Thu) 11:34:37 by Harold Carr.
//

package corba.legacybootstrapserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import corba.framework.Controller;
import corba.framework.Options;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class Client 
{
    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";

    public static final String bootstrapFilename = "boostrapData";
    public static final String orbInitialPort = "4444";
    public static final String initialEntryName = "InitialEntry";

    public static ORB orb;

    public static void main(String av[])
    {
        try {
            System.out.println(main + " starting");
            System.out.println(main + " " + getBootstrapFilePathAndName());

            /* This doesn't work in test framework.  Bug 4970599
            Properties props = new Properties();
            props.setProperty(ORBConstants.INITIAL_PORT_PROPERTY, orbInitialPort);
            */

            String[] args = { "-ORBInitialPort",
                              getORBInitialPort() };

            orb = ORB.init(args, (Properties)null);

            lookup(false, "foo");
            org.omg.CORBA.Object o = lookup(true, initialEntryName);
            update(true, "foo", o);
            lookup(true, "foo");
            // NOTE: without this wait the test fails.
            // Wait a second before removing it so that
            // the file modified time looks diferent to the server
            // from the last update.  
            Thread.sleep(1000);
            update(false, "foo", null);
            lookup(false, "foo");

            System.out.println(main + ": Test complete.");

        } catch (Throwable t) {
            System.out.println(main + ": unexpected exception: " + t);
            t.printStackTrace(System.out);
            System.exit(1);
        }
        System.out.println(main + ": PASSED");
        System.exit(Controller.SUCCESS);
    }

    public static org.omg.CORBA.Object lookup(boolean shouldBeThere, 
                                              String name)
        throws
            Exception
    {
        String filename = getBootstrapFilePathAndName();
        System.out.println(filename
                           + " lookup(" 
                           + (shouldBeThere ? "shouldFind" : "shouldNotFind") + ", "
                           + name + ")");

        org.omg.CORBA.Object o = null;
        try {
            o = orb.resolve_initial_references(name);
            if (! shouldBeThere) {
                throw new Exception("Should not have found: " + name);
            }
            // Use the reference to make sure it works.
            o._non_existent();
            o._is_a("foo");
        } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
            if (shouldBeThere) {
                throw e;
            }
        }
        return o;
    }

    public static String getORBInitialPort()
    {
        return orbInitialPort;
    }

    public static String getBootstrapFilePathAndName()
    {
        return 
            //Options.getOutputDirectory()
            System.getProperty("output.dir")
            + System.getProperty("file.separator")
            + Client.bootstrapFilename;
    }

    public static void update(boolean shouldAdd,
                              String name,
                              org.omg.CORBA.Object o)
        throws
            Exception
    {
        String filename = getBootstrapFilePathAndName();
        System.out.println(filename
                           + " update(" 
                           + (shouldAdd ? "add" : "remove") + ", "
                           + name + ", "
                           + (o == null ? "null" : "IOR") + ")");
        FileInputStream is = new FileInputStream(filename);
        Properties props = new Properties();
        props.load(is);
        is.close();
        if (shouldAdd) {
            props.put(name, orb.object_to_string(o));
        } else {
            props.remove(name);
        }
        writeProperties(props, getBootstrapFilePathAndName());
    }

    public static void writeProperties(Properties props, String filename)
        throws
            Exception
    {
        FileOutputStream os = new FileOutputStream(filename);
        props.store(os, null);
        os.close();
    }
}

// End of file.
