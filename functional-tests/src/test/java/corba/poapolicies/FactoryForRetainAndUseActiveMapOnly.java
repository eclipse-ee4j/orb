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

import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

public class FactoryForRetainAndUseActiveMapOnly implements POAFactory
{

    public POA createPOA(POA parent)
        throws AdapterAlreadyExists, InvalidPolicy
    {
        Policy[] policies = new Policy[2];

        System.out.println("createPOA1");

        policies[0] =
            parent.create_servant_retention_policy(
                ServantRetentionPolicyValue.RETAIN);

        System.out.println("createPOA2");

        policies[1] =
            parent.create_request_processing_policy(
                RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY);


        System.out.println("createPOA3");
        
        POA x = parent.create_POA("RetainAndUseActiveMap",
                                 null,
                                 policies);

        System.out.println("createPOA4");

        return x;
    }

    public String getObjectFactoryName() {
        return "corba.poapolicies.BasicObjectFactoryImpl";
    }
}
