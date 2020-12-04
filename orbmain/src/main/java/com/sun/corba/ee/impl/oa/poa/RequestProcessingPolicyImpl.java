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

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

public class RequestProcessingPolicyImpl
    extends org.omg.CORBA.LocalObject implements RequestProcessingPolicy {

    public RequestProcessingPolicyImpl(RequestProcessingPolicyValue
                                       value) {
        this.value = value;
    }

    public RequestProcessingPolicyValue value() {
        return value;
    }

    public int policy_type()
    {
        return REQUEST_PROCESSING_POLICY_ID.value ;
    }

    public Policy copy() {
        return new RequestProcessingPolicyImpl(value);
    }

    public void destroy() {
        value = null;
    }

    private RequestProcessingPolicyValue value;

    public String toString()
    {
        String type = null ;
        switch (value.value()) {
            case RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY :
                type = "USE_ACTIVE_OBJECT_MAP_ONLY" ;
                break ;
            case RequestProcessingPolicyValue._USE_DEFAULT_SERVANT :
                type = "USE_DEFAULT_SERVANT" ;
                break ;
            case RequestProcessingPolicyValue._USE_SERVANT_MANAGER :
                type = "USE_SERVANT_MANAGER" ;
                break ;
        }

        return "RequestProcessingPolicy[" + type + "]" ;
    }
}
