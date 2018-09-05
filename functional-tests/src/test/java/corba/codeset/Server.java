/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.codeset;

import CodeSetTester.VerifierPOA;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;

public class Server extends VerifierPOA
{
    public char verifyChar (char ch) {
        System.out.println("verifyChar " + (int)ch);
        return ch;
    }

    public char verifyWChar (char wch) {
        System.out.println("verifyWChar " + (int)wch);
        return wch;
    }

    public String verifyString (String str) {
        System.out.println("verifyString " + str);
        return str;
    }

    public String verifyWString (String wstr) {
        System.out.println("verifyWString " + wstr.length());
        return wstr;
    }

    public void verifyCharSeq (CodeSetTester.VerifierPackage.TestCharSeqHolder chSeq) {
        System.out.println("verifyCharSeq " + chSeq.value.length);
    }

    public void verifyWCharSeq (CodeSetTester.VerifierPackage.TestWCharSeqHolder wchSeq) {
        System.out.println("verifyWCharSeq " + wchSeq.value.length);
    }

    public CodeSetTester.CustomMarshaledValue verifyTransmission (CodeSetTester.CustomMarshaledValue cv) {
        return cv;
    }

    public static void main(String args[])
    {
        try {
      
            ORB orb = ORB.init(args, System.getProperties());
            System.out.println(orb);
      
            // Get rootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();
      
            // create servant and register it with the ORB
            Server verifierRef = new Server();
      
            byte[] id = rootPOA.activate_object(verifierRef);
      
            // get the root naming context
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
            // bind the Object Reference in Naming
            NameComponent nc = new NameComponent("Verifier", "");
            NameComponent path[] = {nc};
      
            org.omg.CORBA.Object ref = rootPOA.id_to_reference(id);
            
            ncRef.rebind(path, ref);
            
            // Emit the handshake the test framework expects
            // (can be changed in Options by the running test)
            System.out.println ("Server is ready.");

            // Wait for clients
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
