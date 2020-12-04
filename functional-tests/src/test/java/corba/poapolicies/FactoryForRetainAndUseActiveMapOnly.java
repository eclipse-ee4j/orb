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
