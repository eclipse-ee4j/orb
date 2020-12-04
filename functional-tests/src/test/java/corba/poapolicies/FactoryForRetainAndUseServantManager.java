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

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;

import java.io.*;
import java.util.*;

import Util.*;

public class FactoryForRetainAndUseServantManager implements POAFactory {

//    org.omg.CORBA.ORB Orb;

//    public void setORB(org.omg.CORBA.ORB orb) {
//        Orb = orb;
//    }

    public POA createPOA(POA parent)
        throws AdapterAlreadyExists, InvalidPolicy
    {
        Policy[] policies = new Policy[2];
        policies[0] =
            parent.create_servant_retention_policy(ServantRetentionPolicyValue.RETAIN);
        policies[1] =
            parent.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
        
        POA p = parent.create_POA("RetainAndUseServantManager",
                                  null,
                                  policies);
        try {
            ServantActivatorImpl smi = new ServantActivatorImpl();
            p.activate_object(smi);
            org.omg.CORBA.Object objRef = p.servant_to_reference(smi);
            ServantActivator sl = ServantActivatorHelper.narrow(objRef);
            p.set_servant_manager(sl);
        } catch (WrongPolicy w) {
            System.err.println("Wrong policy in RetainAndUseServantManager POA");
        } catch (Exception exp) {
            System.err.println("Exception RetainAndUseServantManager POA");
        }
        return p;
    }

    public String getObjectFactoryName() {
        return "corba.poapolicies.ObjectFactoryUsingServantManager";
    }
}
