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

package corba.poamanager;

import com.sun.corba.ee.spi.misc.ORBConstants;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;
import org.omg.CORBA.ORB;

public class Utility {
    private ORB orb;
    
    public Utility(String args[]) {
        orb = createORB(args);
    }
    
    private static ORB createORB(String args[]) {
        Properties props = new Properties();
        props.setProperty("org.omg.CORBA.ORBClass",
                  System.getProperty("org.omg.CORBA.ORBClass"));
        props.setProperty(ORBConstants.TRANSPORT_TCP_CONNECT_TIMEOUTS_PROPERTY, "250:2000:100");
        // props.setProperty("com.sun.corba.ee.ORBDebug", "transport,subcontract,poa");
        ORB o = ORB.init(args, props);
        return o;
    }

    public ORB getORB() {
        return this.orb;
    }

    public void writeObjref(org.omg.CORBA.Object ref, String file) {
        String fil = System.getProperty("output.dir")+System.getProperty("file.separator")+file;
        try {
            DataOutputStream out = new 
                DataOutputStream(new FileOutputStream(fil));
            out.writeBytes(orb.object_to_string(ref));
        } catch (java.io.IOException e) {
            System.err.println("Unable to open file "+fil);
            System.exit(1);
        }
    }

    public org.omg.CORBA.Object readObjref(String file) {
        String fil = System.getProperty("output.dir")+System.getProperty("file.separator")+file;
        try {
            BufferedReader in = new BufferedReader(new FileReader(fil));
            String ior = in.readLine();
            System.out.println("IOR: "+ior);
            return orb.string_to_object(ior);
        } catch (java.io.IOException e) {
            System.err.println("Unable to open file "+fil);
            System.exit(1);
        }
        return null;
    }

    public void writeFactory(Util.GenericFactory ref) {
        writeObjref(ref, "Factory");
    }

    public Util.GenericFactory readFactory() {
        return Util.GenericFactoryHelper.narrow(readObjref("Factory"));
    }
}

