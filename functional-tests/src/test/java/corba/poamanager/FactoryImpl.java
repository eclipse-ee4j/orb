/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poamanager;

import Util.CreationMethods;
import Util.GenericFactoryPOA;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.Servant;

public final class FactoryImpl extends GenericFactoryPOA
{
    final boolean useServantToReference = true;

    final String idString = new String("Blue Skies, Black Death");
    
    private POA poa;
    
    public FactoryImpl(POA poa) {
        this.poa = poa;
    }

    public org.omg.CORBA.Object create(String intfName,
                                       String implName, CreationMethods how) {
        try {
            // create Servant first.
            Servant s = (Servant)
                Class.forName(implName).newInstance();

            org.omg.CORBA.Object ref = null;

            switch (how.value()) {
            case Util.CreationMethods._EXPLICIT_ACTIVATION_WITH_POA_ASSIGNED_OIDS:
                {
                    byte[] id = poa.activate_object(s);
                    if (useServantToReference)
                        ref = poa.servant_to_reference(s);
                    else
                        ref = poa.id_to_reference(id);
                }
                break;
            case Util.CreationMethods._EXPLICIT_ACTIVATION_WITH_USER_ASSIGNED_OIDS:
                {
                    byte[] id = idString.getBytes();
                    poa.activate_object_with_id(id, s);
                    if (useServantToReference)
                        ref = poa.servant_to_reference(s);
                    else
                        ref = poa.id_to_reference(id);
                }
                break;
            case Util.CreationMethods._CREATE_REFERENCE_BEFORE_ACTIVATION_WITH_POA_ASSIGNED_OIDS:
                {
                    ref = poa.create_reference(intfName);
                    byte[] id = poa.reference_to_id(ref);
                    poa.activate_object_with_id(id, s);
                }
                break;
            case Util.CreationMethods._CREATE_REFERENCE_BEFORE_ACTIVATION_WITH_USER_ASSIGNED_OIDS:
                {
                    String newIdString = "ABCD";
                    byte[] id = newIdString.getBytes();
                    ref =
                        poa.create_reference_with_id(id, intfName);
                    poa.activate_object_with_id(id, s);
                }
                break;
            }
            return ref;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public void activate() throws AdapterInactive
    {
        poa.the_POAManager().activate();
    }

    public void holdRequests()
    {
        try {
            poa.the_POAManager().hold_requests(true);
        } catch ( Exception ex ) {}
    }

    public void discardRequests()
    {
        try {
            poa.the_POAManager().discard_requests(true);
        } catch ( Exception ex ) {}
    }

    public void deactivate()
    {
        try {
            poa.the_POAManager().deactivate(true, true);
            System.out.println("Deactivated");
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

}
        
            
