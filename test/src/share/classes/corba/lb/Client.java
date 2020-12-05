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

//
// Created       : 2005 Oct 05 (Wed) 14:43:22 by Harold Carr.
// Last Modified : 2005 Oct 06 (Thu) 11:59:21 by Harold Carr.
//

package corba.lb;

import java.util.Hashtable;
import java.util.Properties;
import java.rmi.RemoteException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;

import com.sun.corba.ee.spi.misc.ORBConstants ;

/**
 * @Author Ken Cavanaugh
 * @author Harold Carr
 */
public class Client
{
    static {
        // This is needed to guarantee that this test will ALWAYS use dynamic
        // RMI-IIOP.  Currently the default is dynamic when renamed to "ee",
        // but static in the default "se" packaging, and this test will
        // fail without dynamic RMI-IIOP.
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;
    }

    private static int NUM_ITERATIONS = 1000 ;

    private static int errorCount = 0 ;

    private static InitialContext ic ;

    public static void main(String[] av)
    {
        try {
            Properties props = new Properties() ;
            // props.setProperty("com.sun.corba.ee.ORBDebug","subcontract,transport");
            props.setProperty(ORBConstants.ORB_SERVER_ID_PROPERTY, "100" ) ;
            ORB orb = ORB.init((String[])null, props);

            // See if this reproduces the AmEx problem
            ((com.sun.corba.ee.impl.orb.ORBImpl)orb).getFVDCodeBaseIOR() ;

            Hashtable env = new Hashtable() ;
            env.put( "java.naming.corba.orb", orb ) ;
            ic = new InitialContext(env);

            System.out.println( "Getting test reference" ) ;
            Test ref  = (Test)lookupAndNarrow(Common.ReferenceName, 
                Test.class, ic);

            for (int ctr=0; ctr<NUM_ITERATIONS; ctr++) {
                System.out.print( "Calling echo with argument, " + ctr ) ;

                int result = 0 ;
                try {
                    try {
                        Thread.sleep( 4 ) ;
                    } catch (InterruptedException exc) {
                        System.out.println( "" + exc ) ;
                    }

                    result = ref.echo( ctr ) ;
                    if (result != ctr) {
                        throw new Exception( "Result does not match argument" ) ;
                    } else {
                        System.out.println(", succesfully returned, " + result);
                    }
                } catch (SystemException exc) {
                    System.out.println( "ERROR: " + exc ) ;
                    errorCount++ ;
                    exc.printStackTrace(System.out);
                } catch (RemoteException exc) {
                    System.out.println( "ERROR: " + exc ) ;
                    errorCount++ ;
                    exc.printStackTrace(System.out);
                }
            }

            System.out.println("Loop completed.");
            System.out.println();

            System.out.println("--------------------------------------------");

            if (errorCount == 1) {
                System.out.println("Client failed (" + errorCount + 
                                   ") time(s) due to server restart");
                errorCount = 0 ;
            }

            System.out.println("Client " + ((errorCount==0) ? "SUCCESS" : "FAILURE") );
            System.out.println("--------------------------------------------");
            System.exit(errorCount);

        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("--------------------------------------------");
            System.out.println("Client FAILURE");
            System.out.println("--------------------------------------------");
            System.exit(1);
        }
    }

    public static Object lookupAndNarrow(String name, 
                                         Class clazz,
                                         InitialContext ic )
        throws Exception
    {
        System.out.println( "Looking up " + name ) ;
        Object obj = ic.lookup( name) ;
        System.out.println( "Narrowing object" ) ;
        return PortableRemoteObject.narrow(obj, clazz);
    }
}

// End of file.

