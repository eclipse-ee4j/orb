/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package test12;

import java.util.Properties ;
import org.glassfish.pfl.test.JUnitReportHelper;

public class SerializationTest extends test.Test {
    public void run() {
        JUnitReportHelper helper = new JUnitReportHelper( SerializationTest.class.getName() ) ;

        try {        
            helper.start( "test1" ) ;
            Properties props = new Properties() ;
            props.put( "org.omg.CORBA.ORBClass", "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            org.omg.CORBA.ORB orb = 
                org.omg.CORBA.ORB.init(getArgsAsArgs(),props);
            org.omg.CORBA_2_3.portable.OutputStream sos =
                (org.omg.CORBA_2_3.portable.OutputStream)orb.create_output_stream();


            ARectangle rect = new ARectangle(1,3,5,7);
            sos.write_value(rect);

            /***************************************************************/
            /*********************** READ DATA BACK IN *********************/
            /***************************************************************/

            org.omg.CORBA_2_3.portable.InputStream sis = 
                (org.omg.CORBA_2_3.portable.InputStream)sos.create_input_stream();

            ARectangle _rect = (ARectangle)sis.read_value();
            if (!rect.equals(_rect))
                throw new Error("ARectangle test failed!");

            helper.pass() ;
        } catch(Throwable e) {
            helper.fail( e ) ;
            status = new Error(e.getMessage());
            e.printStackTrace();
        } finally {
            helper.done() ;
        }
    }
}
