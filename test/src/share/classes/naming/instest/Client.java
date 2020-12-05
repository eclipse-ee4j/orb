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



