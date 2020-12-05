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

package corba.lb ;

import java.util.Properties ;

import corba.framework.CORBATest ;
import corba.framework.Options ;
import corba.framework.Controller ;


import com.sun.corba.ee.spi.misc.ORBConstants ;
import com.sun.corba.ee.impl.plugin.hwlb.VirtualAddressAgentImpl ;
import com.sun.corba.ee.impl.plugin.hwlb.NoConnectionCacheImpl ;

public class LBTest
    extends
        CORBATest
{
    private static final String LB_HOST = "localhost" ;
    private static final int LB_PORT = 57340 ;
    private static final int S1_PORT = 57351 ;
    private static final int S2_PORT = 57352 ;

    private static final int SHARED_SERVER_ID = 2727 ;

    protected void doTest()
        throws Exception
    {
        String thisPackage = this.getClass().getPackage().getName() ;
        String pluginPackage = "com.sun.corba.ee.impl.plugin.hwlb" ;

        // Set up shared client and server properties.  This causes the client
        // ORBs to be initialized without connection caching, and the server
        // ORBs to use ORT to set the server port to Sx_PORT, while creating
        // IORs that contains the LB_PORT.
        Properties serverProps = Options.getServerProperties() ; 
        serverProps.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY, 
            Integer.toString(SHARED_SERVER_ID)) ;
        serverProps.setProperty( ORBConstants.USER_CONFIGURATOR_PREFIX 
            + pluginPackage + "." + "VirtualAddressAgentImpl",
            "dummy" ) ;
        serverProps.setProperty( VirtualAddressAgentImpl.VAA_HOST_PROPERTY, 
            LB_HOST ) ;
        serverProps.setProperty( VirtualAddressAgentImpl.VAA_PORT_PROPERTY, 
            Integer.toString(LB_PORT) ) ;

        Properties clientProps = Options.getClientProperties() ; 
        clientProps.setProperty( ORBConstants.USER_CONFIGURATOR_PREFIX 
                + pluginPackage + "." + "NoConnectionCacheImpl",
                "dummy" ) ;
        
        Controller orbd = createORBD();
        orbd.start();

        Controller lb;
        Controller server1;
        Controller server2;
        Controller client;

        String lbArgs = "-listen " + LB_PORT + " -pool " + S1_PORT + " " + S2_PORT ;
        Options.addServerArgs( lbArgs ) ;

        lb = createServer(thisPackage+"."+"LB", "LB" ) ;

        serverProps.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, 
            Integer.toString(S1_PORT)) ;
        server1 = createServer(thisPackage+"."+"Server", "Server1.1");

        serverProps.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, 
            Integer.toString(S2_PORT)) ;
        server2 = createServer(thisPackage+"."+"Server", "Server2");
        
        client = createClient(thisPackage+"."+"Client", "Client");

        lb.start() ;
        Thread.sleep( 1000 ) ;

        server1.start();
        server2.start();

        Thread.sleep( 1000 ) ;

        client.start();

        // Wait for client to get started before stopping server1.
        Thread.sleep( 4000 ) ;
        server1.stop();

        Thread.sleep( 1000 ) ;
        serverProps.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, 
            Integer.toString(S1_PORT)) ;
        server1 = createServer(thisPackage+"."+"Server", "Server1.2");
        server1.start() ;

        client.waitFor(1000 * 60 * 2);

        client.stop();
        lb.stop() ;
        server1.stop();
        server2.stop();
        orbd.stop();
    }
}
