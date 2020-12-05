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
        
            
