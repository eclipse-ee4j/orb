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
