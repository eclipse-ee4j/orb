/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poapolicies;

import Util.CreationMethods;
import Util.FactoryPOA;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

public class BasicObjectFactoryImpl extends FactoryPOA 
{
    final boolean useServantToReference = true;

    final String idString = new String("Blue Skies, Black Death");
    
    POA poa;

    public java.lang.Object doneCV = new java.lang.Object();
    
    void setPOA(POA p) {
        poa = p;
    }

    public void overAndOut() {
        synchronized (doneCV) {
            doneCV.notifyAll();
        }
    }
    
    public org.omg.CORBA.Object create(String intfName,
                                       String implName, CreationMethods how) {
        try {

            System.err.println("Creating: " + implName);

            // create Servant first.

            Servant s;
            try {
            s = (Servant)
                Class.forName(implName).newInstance();
            } catch (Exception ex) {
                System.err.println("Problems finding: " + implName);
                ex.printStackTrace();
                System.err.println("---");
                throw ex;
            }

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
            System.err.println("BasicObjectFactoryImpl");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
        
            
