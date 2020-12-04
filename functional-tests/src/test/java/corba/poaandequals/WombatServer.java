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

package corba.poaandequals;

import WombatStuff.WombatHelper;
import corba.framework.Controller;
import corba.framework.InternalProcess;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;

public class WombatServer implements InternalProcess 
{
    PrintStream out;
    PrintStream err;
    ORB orb;

    static String root = "RootPOA";

    public void writeObjref(org.omg.CORBA.Object ref, 
                            String file,
                            String outputDir) throws java.io.IOException {
        String fil = outputDir
            + File.separator
            + file;

        java.io.DataOutputStream outstr = new 
            java.io.DataOutputStream(new FileOutputStream(fil));
        outstr.writeBytes(orb.object_to_string(ref));
    }

    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        this.out = out;
        this.err = err;
        JUnitReportHelper helper = new JUnitReportHelper( WombatServer.class.getName() ) ;

        try {
            Controller client = (Controller)extra.get("client");
            orb = (ORB)extra.get("orb");

            out.println("Running server");

            POA poa = null;
            try {
                poa = (POA) orb.resolve_initial_references(root);
            } catch (InvalidName name) {
                err.println(root + " is an invalid name");
                throw name;
            }

            out.println("Activating servant...");

            WombatImpl w = new WombatImpl("BooBoo");
            byte[] id = null;
            try {
                helper.start( "ActivationTest" ) ;
                id = poa.activate_object(w);
                writeObjref(poa.create_reference_with_id(id, 
                    WombatHelper.id()), "WombatObjRef",
                    environment.getProperty("output.dir"));
                poa.the_POAManager().activate();
                helper.pass() ;
            } catch (Exception ex) {
                err.println(root+" threw "+ex+" after activate_object");
                helper.fail( ex ) ;
                throw ex;
            }

            out.println("Activated object, starting client");
        
            client.start();
            client.waitFor();

            out.println("Client finished, deactivating object");

            try {
                helper.start( "DeactivationTest" ) ;
                poa.deactivate_object(id);
                helper.pass() ;
            } catch (Exception ex) {
                err.println(root+" threw "+ex+" in deactivate_object");
                helper.fail( ex ) ;
                throw ex;
            }

            out.println("Destroying poa");
            
            poa.destroy(true, false);

            out.println("Finished");
        } finally {
            helper.done() ;
        }
    }
}
