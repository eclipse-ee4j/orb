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

package corba.cmvt;

import org.omg.CORBA.portable.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.Properties;
import org.omg.PortableServer.*;
import java.io.*;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

public class Server
{
    public static void writeObjref(org.omg.CORBA.Object ref, String file, org.omg.CORBA.ORB orb) {
        String fil = System.getProperty("output.dir")+System.getProperty("file.separator")+file;
        try {
            java.io.DataOutputStream out = new 
                java.io.DataOutputStream(new FileOutputStream(fil));
            out.writeBytes(orb.object_to_string(ref));
        } catch (java.io.IOException e) {
            System.err.println("Unable to open file "+fil);
            System.exit(1);
        }
    }

    public static void main(String args[])
    {
        try {
      
            ORB orb = ORB.init(args, System.getProperties());

            com.sun.corba.ee.spi.orb.ORB ourORB
                = (com.sun.corba.ee.spi.orb.ORB)orb;

            System.out.println("==== Server GIOP version "
                               + ourORB.getORBData().getGIOPVersion()
                               + " with strategy "
                               + ourORB.getORBData().getGIOPBuffMgrStrategy(
                                    ourORB.getORBData().getGIOPVersion())
                               + "====");
      
            // Get rootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();

            GIOPComboImpl impl = new GIOPComboImpl();
            javax.rmi.CORBA.Tie tie = javax.rmi.CORBA.Util.getTie( impl ) ; 

            byte[] id = rootPOA.activate_object( 
                                                 (org.omg.PortableServer.Servant)tie ) ;
            org.omg.CORBA.Object obj = rootPOA.id_to_reference( id ) ;


            writeObjref(obj, "IOR", orb);

            System.out.println ("Server is ready.");

            orb.run();
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);

            // Make sure to exit with a value greater than 0 on
            // error.
            System.exit(1);
        }
    }
}
