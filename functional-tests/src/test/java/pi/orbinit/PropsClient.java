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

package pi.orbinit;

import org.omg.CORBA.*;
import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.spi.misc.ORBConstants;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.*;
import corba.framework.*;

import java.util.*;
import java.io.*;

/**
 * Client that passes in orb initializers as a properties object
 */
public class PropsClient 
    extends ClientCommon
{
    public static void main(String args[]) {
        try {
            (new PropsClient()).run( System.getProperties(),
                                     args, System.out, System.err, null );
        }
        catch( Exception e ) {
            e.printStackTrace( System.err );
            System.exit( 1 );
        }
    }

    protected ORB createORB( String[] args ) {
        // Initializer classes
        String invalidInitializer = "com.sun.nonexistent.intializer.Foo";
        String testInitializer = "pi.orbinit.ClientTestInitializer";

        // add an additional argument to args[].
        String[] newArgs = new String[ args.length + 2 ];
        int i = 0;
        for( i = 0; i < args.length; i++ ) {
            newArgs[i] = args[i];
        }

        // We will check for the presence of these arguments later:
        newArgs[i++] = "abcd";
        newArgs[i++] = "efgh";

        // create and initialize the ORB
        Properties props = new Properties() ;
        props.put( "org.omg.CORBA.ORBClass", 
                   System.getProperty("org.omg.CORBA.ORBClass"));
        props.put( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX + 
            invalidInitializer, "" );
        props.put( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX + 
            testInitializer, "" );

        return ORB.init(newArgs, props);
    }

}
