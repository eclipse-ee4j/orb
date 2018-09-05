/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.preinvokepostinvoke;

public class TestAssert {

    private static int preInvokeCounter = 0;
    private static int postInvokeCounter = 0;

    public static synchronized void startTest( ) { 
        preInvokeCounter = 0;
        postInvokeCounter = 0;
    }

    public static synchronized void preinvokeCalled( ) { 
        preInvokeCounter++;
    }

    public static synchronized void postinvokeCalled( ) { 
        if( preInvokeCounter == 0 ) {
            System.err.println( "ServantLocator.post_invoke() called before " +
               " calling pre_invoke" );
            System.exit( 1 );
        }
        postInvokeCounter++;
    }

    public static synchronized void isTheCallBalanced( int expectedCount ) { 
        if ((preInvokeCounter != expectedCount  )
          ||(postInvokeCounter != expectedCount ) )
        {
            throw new RuntimeException(
                    "Test Failed because the preinvoke and " +
                    "postinvoke call is not balanced using " +
                    " FullServantCaching policy." +
                    "\n preInvokeCounter = " + preInvokeCounter +
                    "\n postInvokeCounter = " + postInvokeCounter +
                    "\n expectedCount = " + expectedCount );
        }
    }
}
