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
// Created       : 1999 by Harold Carr.
// Last Modified : 2004 Apr 29 (Thu) 16:28:35 by Harold Carr.
//

package corba.connectintercept_1_4;

import com.sun.corba.ee.spi.activation.IIOP_CLEAR_TEXT;
import com.sun.corba.ee.spi.activation.EndPointInfo;
import com.sun.corba.ee.spi.activation.Locator;
import com.sun.corba.ee.spi.activation.LocatorHelper;
import com.sun.corba.ee.spi.activation.LocatorPackage.ServerLocationPerORB;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.ior.ObjectKey ;
import com.sun.corba.ee.spi.ior.IORFactories ;
import com.sun.corba.ee.spi.ior.ObjectKeyFactory ;

import com.sun.corba.ee.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion; 

import com.sun.corba.ee.spi.protocol.ForwardException; 

import com.sun.corba.ee.spi.orb.ORB; 

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.ORBUtility;

import com.sun.corba.ee.impl.ior.IORImpl;
import com.sun.corba.ee.impl.ior.POAObjectKeyTemplate ;

import com.sun.corba.ee.impl.oa.poa.BadServerIdHandler;

import com.sun.corba.ee.impl.util.Utility;

import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.OBJECT_NOT_EXIST;

public class ORBDBadServerIdHandler
    implements
        BadServerIdHandler
{
    public static final String baseMsg =
        ORBDBadServerIdHandler.class.getName();

    private ORB orb;

    public ORBDBadServerIdHandler(org.omg.CORBA.ORB orb)
    {
        this.orb = (com.sun.corba.ee.spi.orb.ORB) orb;
    }

    public void handle(ObjectKey okey)
    {
        Locator locator = null;
        try {
            locator = LocatorHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_LOCATOR_NAME));
        } catch (InvalidName ex) {
            // Should never happen.
            System.out.println("ORBDBadServerIdHandler.handle: " + ex);
            System.exit(-1);
        }

        IOR newIOR = null;
        ServerLocationPerORB location;

        POAObjectKeyTemplate poktemp = (POAObjectKeyTemplate)
            (okey.getTemplate());
        int serverId = poktemp.getServerId() ;
        String orbId = poktemp.getORBId() ;

        try {
            location  = locator.locateServerForORB(serverId, orbId);

            int clearPort = 
                locator.getServerPortForType(location, IIOP_CLEAR_TEXT.value);

            int myType1Port
                = locator.getServerPortForType(location, Common.MyType1);
            int myType2Port
                = locator.getServerPortForType(location, Common.MyType2);
            int myType3Port
                = locator.getServerPortForType(location, Common.MyType3);

            String componentData =
                Common.createComponentData(baseMsg + ".handle: ",
                                           myType1Port,
                                           myType2Port,
                                           myType3Port);

            /*
              1. Use ObjectKeyFactory.create( byte[]) to convert byte[]
              object key to ObjectKey (if it's not already ObjectKey).
              Note that the arg type will change to a stream in my next
              putback.
              2. Use host and port to construct an IIOPAddress.
              3. Use address from 2, object key template from 1, and GIOP
              version info to construct IIOPProfileTemplate.
              4. Add tagged components to IIOPProfileTemplate.
              5. Use IIOPProfileTemplate from 4 and ObjectId from 1
              to construct IIOPProfile.
              6. Construct IOR from ORB and repid.
              7. Add IIOPProfile to IOR.
              8. Make IOR immutable.
            */

            IIOPProfileTemplate sipt = 
                IIOPFactories.makeIIOPProfileTemplate(
                    (com.sun.corba.ee.spi.orb.ORB)orb,
                    GIOPVersion.V1_2,
                    IIOPFactories.makeIIOPAddress( location.hostname, clearPort));
            sipt.add(new ORBDListenPortsComponent(componentData));
            IORTemplate iortemp = IORFactories.makeIORTemplate( poktemp ) ;
            iortemp.add( sipt ) ;
            newIOR = iortemp.makeIOR( (com.sun.corba.ee.spi.orb.ORB)orb, 
                "IDL:org/omg/CORBA/Object:1.0", okey.getId() );

            /*
            // REVISIT - add component data.

            newIOR = new IOR((com.sun.corba.ee.spi.orb.ORB)orb,
                             "IDL:org/omg/CORBA/Object:1.0",
                             location.hostname,
                             myType2Port, // REVISIT - clearPort
                             objectKey);
            */
        } catch (Exception e) {
            // For this example, all exceptions map to:
            throw new OBJECT_NOT_EXIST();
        }

        throw new ForwardException( (com.sun.corba.ee.spi.orb.ORB)orb, newIOR);
    }
}

// End of file.
