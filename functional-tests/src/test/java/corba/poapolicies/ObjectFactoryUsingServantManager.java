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

import Util.CreationMethods ;

public class ObjectFactoryUsingServantManager extends BasicObjectFactoryImpl {
    private static int idNum = 0;

    @Override
    public org.omg.CORBA.Object create(String intfName,
                                       String implName,
                                       CreationMethods how) {
        
        String objectId = "ObjectID" + idNum++;

        try {
            byte[] id = objectId.getBytes();

            return poa.create_reference_with_id(id, intfName);
        } catch (Exception e) {
            System.err.println("ObjectFactoryUsingServantManager");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
