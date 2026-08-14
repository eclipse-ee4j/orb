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

/* This will print the CORBA RepositoryId for a specified Java class.
 *
 * If the rules laid down in the CORBA spec (section 10.6.2) and the
 * CORBA Java-to-IDL spec (section 1.3.5.6) are followed, I believe
 * the following results should be obtained:
 *
 *
 * java ShowRepid java.lang.Class
 * class java.lang.Class = RMI:javax.rmi.CORBA.ClassDesc:2BABDA04587ADCCC:CFBF02CF5294176B
 *
 * java ShowRepid java.util.GregorianCalendar
 * class java.util.GregorianCalendar = RMI:java.util.GregorianCalendar:450042FBA7A923B1:8F3DD7D6E5B0D0C1
 *
 * java ShowRepid java.io.ObjectStreamClass
 * class java.io.ObjectStreamClass = RMI:java.io.ObjectStreamClass:071DA8BE7F971128:AB0E6F1AEEFE7B88
 *
 * java ShowRepid ShowRepid
 * class ShowRepid = RMI:ShowRepid:AC117E28FE36587A:0000000000001234
 */
package corba.serialization.good;

import java.io.*;
import com.sun.corba.ee.impl.util.RepositoryId;

public class ShowRepid implements Serializable {
    static final long serialVersionUID = 0x1234;

    private void writeObject(ObjectOutputStream s) throws IOException {
    }

    private static int runTest() {
        int rc = 0;

        String r1 = "RMI:javax.rmi.CORBA.ClassDesc:2BABDA04587ADCCC:CFBF02CF5294176B";
        String r2 = "RMI:java.util.GregorianCalendar:450042FBA7A923B1:8F3DD7D6E5B0D0C1";
        String r3 = "RMI:java.io.ObjectStreamClass:071DA8BE7F971128:AB0E6F1AEEFE7B88";
        String r4 = "RMI:corba.serialization.good.ShowRepid:AC117E28FE36587A:0000000000001234";
        String r5 = "RMI:java.util.Hashtable:86573568A211C011:13BB0F25214AE4B8";

        String s1 = RepositoryId.createForAnyType(java.lang.Class.class);
        String s2 = RepositoryId.createForAnyType(java.util.GregorianCalendar.class);
        String s3 = RepositoryId.createForAnyType(java.io.ObjectStreamClass.class);
        String s4 = RepositoryId.createForAnyType(ShowRepid.class);
        String s5 = RepositoryId.createForAnyType(java.util.Hashtable.class);

        if (!s1.equals(r1)) {
        System.out.println("mismatch " + s1);
        ++rc;
        }
        if (!s2.equals(r2)) {
        System.out.println("mismatch " + s2);
        ++rc;
        }
        if (!s3.equals(r3)) {
        System.out.println("mismatch " + s3);
        ++rc;
        }
        if (!s4.equals(r4)) {
        System.out.println("mismatch " + s4);
        ++rc;
        }
        if (!s5.equals(r5)) {
        System.out.println("mismatch " + s5);
        ++rc;
        }


        return rc;
    }

    public static void main(String[] args) {
        System.out.println("Server is ready.");
        if (args.length == 0) {
            if (runTest() == 0)
                System.out.println("Test PASSED");
            else {
                System.out.println("Test FAILED");
                System.exit(1) ;
            }
        } else {
            try {
                Class clz = Class.forName(args[0]);
                System.out.print(clz + " = ");
                System.out.println(RepositoryId.createForAnyType(clz));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
