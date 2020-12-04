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
// Created       : 2005 Jul 29 (Fri) 08:23:33 by Harold Carr.
// Last Modified : 2005 Sep 23 (Fri) 15:08:31 by Harold Carr.
//

package corba.folb;

import java.util.List;

import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.impl.folb.CSIv2SSLTaggedComponentHandler;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.orb.DataCollector;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.ORBUtility;

/**
 * @author Harold Carr
 */
public class CSIv2SSLTaggedComponentHandlerImpl
    extends org.omg.CORBA.LocalObject
    implements CSIv2SSLTaggedComponentHandler,
               ORBConfigurator
{
    private boolean debug = true;
    private ORB orb;

    ////////////////////////////////////////////////////
    //
    // CSIv2SSLTaggedComponentHandler
    //

    public TaggedComponent insert(IORInfo iorInfo, 
                                  List<ClusterInstanceInfo> clusterInstanceInfo)
    {
        if (debug) { dprint(".insert: " + iorInfo); }
        return null;
    }

    public List<SocketInfo> extract(IOR ior)
    {
        if (debug) { dprint(".extract"); }
        return null;
    }

    ////////////////////////////////////////////////////
    //
    // ORBConfigurator
    //

    public void configure(DataCollector collector, ORB orb) 
    {
        if (debug) { dprint(".configure->:"); }

        this.orb = orb;
        try {
            orb.register_initial_reference(
                ORBConstants.CSI_V2_SSL_TAGGED_COMPONENT_HANDLER,
                this);
        } catch (InvalidName e) {
            dprint(".configure: !!!!! FAILURE");
            e.printStackTrace(System.out);
            System.exit(1);
        }

        if (debug) { dprint(".configure<-:"); }
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    private static void dprint(String msg)
    {
        ORBUtility.dprint("CSIv2SSLTaggedComponentHandlerImpl", msg);
    }

}

// End of file.


