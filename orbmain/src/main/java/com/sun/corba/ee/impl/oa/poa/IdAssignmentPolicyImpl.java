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

package com.sun.corba.ee.impl.oa.poa;

import org.omg.CORBA.Policy;
import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;
import org.omg.PortableServer.IdAssignmentPolicyValue;

final class IdAssignmentPolicyImpl
extends org.omg.CORBA.LocalObject
implements org.omg.PortableServer.IdAssignmentPolicy {

    private static final long serialVersionUID = 7270607036354165654L;

    public IdAssignmentPolicyImpl(IdAssignmentPolicyValue value) {
        this.value = value;
    }

    @Override
    public IdAssignmentPolicyValue value() {
        return value;
    }

    @Override
    public int policy_type()
    {
        return ID_ASSIGNMENT_POLICY_ID.value ;
    }

    @Override
    public Policy copy() {
        return new IdAssignmentPolicyImpl(value);
    }

    @Override
    public void destroy() {
        value = null;
    }

    private IdAssignmentPolicyValue value;

    @Override
    public String toString()
    {
        return "IdAssignmentPolicy[" +
            ((value.value() == IdAssignmentPolicyValue._USER_ID) ?
                "USER_ID" : "SYSTEM_ID" + "]") ;
    }
}
