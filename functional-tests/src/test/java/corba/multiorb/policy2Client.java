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

package corba.multiorb;

import examples.policy_2;
import examples.policy_2Helper;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

public class policy2Client {
    private static final String msgPassed = "policy_2: **PASSED**";
    
    private static final String msgFailed = "policy_2: **FAILED**";
    
    public static void main( String args[] ) {
        JUnitReportHelper helper = new JUnitReportHelper( policy2Client.class.getName() ) ;

        try {
            helper.start( "TwoORBTest" ) ;
            System.out.println( "POLICIES : ORB_CTRL_MODEL,PERSISTENT,UNIQUE_ID,SYSTEM_ID,RETAIN,USE_ACTIVE_OBJECT_MAP_ONLY,NO_IMPLICIT_ACTIVATION" );
            System.out.println( "Starting client" );
            System.out.println( "ORB Initializing" );
            Properties props = new Properties();
            props.put( "org.omg.corba.ORBClass", System.getProperty("org.omg.CORBA.ORBClass"));
            props.setProperty( "com.sun.corba.ee.ORBid", "sunorb1");
            System.out.println("com.sun.corba.ee.ORBid " + props.getProperty("com.sun.corba.ee.ORBid"));
            ORB orb1 = ORB.init( args, props );

            props = new Properties();
            props.put( "org.omg.corba.ORBClass", System.getProperty("org.omg.CORBA.ORBClass"));
            props.setProperty( "com.sun.corba.ee.ORBid", "sunorb2");
            System.out.println("com.sun.corba.ee.ORBid " + props.getProperty("com.sun.corba.ee.ORBid"));
            ORB orb2 = ORB.init( args, props );

            lookupAndInvoke(orb1, "Object1");
            lookupAndInvoke(orb2, "Object2");
            helper.pass() ;
        } catch( Exception exp ) {
            exp.printStackTrace();
            System.out.println( msgFailed + "\n" );
            helper.fail( exp ) ;
        } finally {
            helper.done() ;
        }
    }

    public static void lookupAndInvoke(org.omg.CORBA.ORB orb, String ObjName) throws Exception {
        try {
            System.out.println( "Looking for naming Service" );
            org.omg.CORBA.Object objRef = orb.resolve_initial_references( "NameService" );
            NamingContext ncRef = NamingContextHelper.narrow( objRef );
            System.out.println( "Getting Object Reference" );
            NameComponent nc = new NameComponent( ObjName, "" );
            NameComponent path[] = { nc };
            policy_2 Ref = policy_2Helper.narrow( ncRef.resolve( path ) );
            int l = Ref.increment();
            System.out.println( "Incremented value:" + l );
            System.out.println( msgPassed + "\n" );
        } catch( Exception exp ) {
            throw exp;
        }
    }
}
