/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package naming.instest;

import java.util.Properties;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;

public class Client implements Runnable {

    private String[] args;

    public Client(String[] args) {
        this.args = args;
    }

    public void signalError () {
        System.exit(1);
    }


    public static void main(String args[]) {
        new Client(args).run();
    }

    public void run()
    {
        try {
            Properties props = new Properties() ;
            //props.setProperty( "com.sun.corba.ee.ORBDebug", 
                //"subcontract,transport,naming,serviceContext,transientObjectManager" ) ;
            ORB orb = ORB.init( args, props );

            String corbalocURL
                = System.getProperty(TestConstants.URL_PROPERTY);

            Object obj 
                = orb.string_to_object(corbalocURL);

            if( obj == null ) {
                System.err.println( "string_to_object(" + 
                    corbalocURL + ") failed.." );
                System.err.flush();
                signalError ();
            }
            Hello helloRef = HelloHelper.narrow( obj );
            String returnString = helloRef.sayHello( );
            if( !returnString.equals( TestConstants.returnString ) ) {
                System.err.println( " hello.sayHello() did not return.." +
                    TestConstants.returnString );
                System.err.flush( );
                signalError( );
            }
            System.out.println( "INS Test Passed.." );
        } catch (Exception e ) {
             e.printStackTrace( System.err );
             signalError( );
        }
        System.out.println("Thread "+ Thread.currentThread()+" done.");
    }
}



