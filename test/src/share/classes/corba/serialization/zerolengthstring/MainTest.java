/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/**
 * A Simple test to check copy created from Util.copyObject() preserves the
 * object structure correctly. This test was added to test the bug fix for
 * a P2 Bug (4728756), Util.copyObjects () use to fail when there were 2 fields
 * with different Zero length string instances and 2 other fields aliasing 
 * to those two Strings.  
 */
package corba.serialization.zerolengthstring;

import java.util.Properties;
import org.omg.CORBA.ORB;
import javax.rmi.CORBA.Util;

public class MainTest {

    private static boolean runTest( String[] args ) {
        try {
            ORB orb = ORB.init( args, null );
            ClassWithZeroLengthStrings object = 
                new ClassWithZeroLengthStrings();
            ClassWithZeroLengthStrings copiedObject =
                (ClassWithZeroLengthStrings) Util.copyObject( object, orb );
            // After copying the object successfully, check to see if
            // the structure is the same as expected.
            return copiedObject.validateObject( );
        } catch ( Exception e ) {
            System.err.println( "Exception " + e + " caught in runTest() " );
            e.printStackTrace( );
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("Server is ready.");
        if ( runTest( args ) )
            System.out.println("Test PASSED");
        else {
            System.out.println("Test FAILED");
            System.exit(1) ;
        }
    }
}
