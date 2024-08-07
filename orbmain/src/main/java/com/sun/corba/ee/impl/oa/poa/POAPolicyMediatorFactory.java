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

import com.sun.corba.ee.spi.logging.POASystemException;

abstract class POAPolicyMediatorFactory {
    private static final POASystemException wrapper = POASystemException.self;

    // create an appropriate policy mediator based on the policies.
    // Note that the policies object has already been validated before
    // this call, so it can only contain valid combinations of POA policies.
    static POAPolicyMediator create(Policies policies, POAImpl poa) {
        if (policies.retainServants()) {
            if (policies.useActiveMapOnly()) {
                return new POAPolicyMediatorImpl_R_AOM(policies, poa);
            } else if (policies.useDefaultServant()) {
                return new POAPolicyMediatorImpl_R_UDS(policies, poa);
            } else if (policies.useServantManager()) {
                return new POAPolicyMediatorImpl_R_USM(policies, poa);
            } else {
                throw wrapper.pmfCreateRetain();
            }
        } else {
            if (policies.useDefaultServant()) {
                return new POAPolicyMediatorImpl_NR_UDS(policies, poa);
            } else if (policies.useServantManager()) {
                return new POAPolicyMediatorImpl_NR_USM(policies, poa);
            } else {
                throw wrapper.pmfCreateNonRetain();
            }
        }
    }
}
