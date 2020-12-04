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

package naming.rinameservice;

import com.sun.corba.ee.impl.naming.cosnaming.TransientNameService;
import com.sun.corba.ee.spi.misc.ORBConstants;
import org.omg.CORBA.ORB;
import corba.framework.*;
import java.util.*;
import java.io.*;

/** 
 * This is a simple test to demonstrate the NameService that we ship with RI
 * works. It
 * 1. Instantiates ORB by passing Persistent Port property so that there is
 *    is a listener on port 1050
 * 2. Instantiates TransientNameService by passing the ORB
 */
public class NameServer implements InternalProcess
{

    public static void main( String args[] ) {
        try {
            (new NameServer()).run( System.getProperties(),
                                args, System.out, System.err, null );
        } catch( Exception e ) {
            e.printStackTrace( System.err );
            System.exit( 1 );
        }
    }

    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        try {
            Properties orbProperties = new Properties( );
            orbProperties.put( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
                TestConstants.RI_NAMESERVICE_PORT );
            orbProperties.put( "org.omg.CORBA.ORBClass",
                       "com.sun.corba.ee.impl.orb.ORBImpl" );
            orbProperties.setProperty( ORBConstants.DEBUG_PROPERTY, "subcontract,giop,transport" ) ;
            ORB orb = ORB.init( args, orbProperties );
            TransientNameService standaloneNameService = 
                new TransientNameService( 
                    (com.sun.corba.ee.spi.orb.ORB)orb );
            System.out.println( "Server is ready." ) ;
            orb.run( );
        } catch( Exception e ) {
            System.err.println( "Exception In NameServer " + e );
            e.printStackTrace( );
            System.exit( 1 );
        }
    }
}
