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

package corba.giopgen;

import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;

import com.sun.corba.ee.spi.misc.ORBConstants ;

public class Client
{
    static {
        // This is needed to guarantee that this test will ALWAYS use dynamic
        // RMI-IIOP.  Currently the default is dynamic when renamed to "ee",
        // but static in the default "se" packaging, and this test will
        // fail without dynamic RMI-IIOP.
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;
    }

    private static final int NUM_CALLS = 5 ;

    public static void main(String[] av)
    {
        try {
            Properties props = new Properties();

            ORB orb = ORB.init((String[])null, props);
            Hashtable env = new Hashtable();
            env.put("java.naming.corba.orb", orb);
            InitialContext initialContext = new InitialContext(env);

            Test ref  = (Test)
                lookupAndNarrow(Common.ReferenceName, Test.class, 
                                initialContext);

            String test = "This is a very long string that will be repeatedly concatenated during this test" ;

            for (int ctr=0; ctr<NUM_CALLS; ctr++) {
                short[] arr = new short[100*ctr+1] ;
                for (int ctr2=0; ctr2<arr.length; ctr2++)
                    arr[ctr2] = (short)ctr2 ;
                Map map = new HashMap() ;
                map.put( test, test ) ;
                map.put( "alias1", test ) ;
                map.put( "alias2", test ) ;
                map.put( "alias3", test ) ;
                map.put( "map", map ) ;
                int result = ref.echo(ctr, ctr, arr, test, map );
                if (result != ctr) {
                    throw new Exception("incorrect echo");
                }
                System.out.println("correct echo response: " + result);
                test = test + "*" + test ;
            }

            try {
                System.out.println( "Testing exception context" ) ;
                ref.testExceptionContext() ;
            } catch (Exception exc) {
                System.out.println( "Exception on testExceptionContext: " + exc ) ;
                exc.printStackTrace() ;
            }

            try {
                System.out.println( "Testing simple exception context" ) ;
                ref.testSimpleExceptionContext() ;
            } catch (Exception exc) {
                System.out.println( "Exception on testExceptionContext: " + exc ) ;
                exc.printStackTrace() ;
            }
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
                                         InitialContext initialContext)
        throws
            NamingException
    {
        return PortableRemoteObject.narrow(initialContext.lookup(name), clazz);
    }
}

// End of file.

