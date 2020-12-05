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

package corba.repid;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.misc.ORBConstants ;

public class Client {
    
    public static void main(String args[]) {

        try {

            Properties props = new Properties(System.getProperties());
            props.setProperty(ORBConstants.USE_REP_ID, "false");
            ORB orb = ORB.init(args, props);

            com.sun.corba.ee.spi.orb.ORB ourORB
                = (com.sun.corba.ee.spi.orb.ORB) orb;

            if (ourORB.getORBData().useRepId() != false) {
                throw new RuntimeException("ORB.useRepId flag is not false");
            }

            String[] inStrSeq = new String[] { "Hello", "World" };
            OutputStream ostr = (org.omg.CORBA_2_3.portable.OutputStream)
                                    ourORB.create_output_stream();
            ostr.write_value(inStrSeq, new test.StringSeqHelper());
            InputStream istr = (org.omg.CORBA_2_3.portable.InputStream)
                ostr.create_input_stream();
            String[] outStrSeq = (String[]) 
                istr.read_value(new test.StringSeqHelper());
            for (int i = 0; i < outStrSeq.length; i++) {
                if (!(outStrSeq[i].equals(inStrSeq[i]))) {
                    throw new RuntimeException("Input/output value mismatch");
                }
            }

        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
