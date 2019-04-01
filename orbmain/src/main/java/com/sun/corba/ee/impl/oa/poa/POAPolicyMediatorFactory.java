/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
