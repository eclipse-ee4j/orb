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
