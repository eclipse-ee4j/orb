/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.corba;

import java.security.BasicPermission;

/**
 * Permission class used to protect access to the sun.corba.Bridge object. The only name valid here is "getBridge". The
 * BridgePermission("getBridge") permission must be held by the caller of sun.corba.Bridge.get().
 */
public final class BridgePermission extends BasicPermission {
    /**
     * Creates a new BridgePermission with the specified name. The name is the symbolic name of the BridgePermission. The
     * only valid name here is "getBridge".
     *
     * @param name the name of the BridgePermission.
     */
    public BridgePermission(String name) {
        super(name);
    }

    /**
     * Creates a new BridgePermission object with the specified name. The name is the symbolic name of the BridgePermission,
     * and the actions String is currently unused and should be null. The only valid name here is "getBridge".
     *
     * @param name the name of the BridgePermission.
     * @param actions should be null.
     */

    public BridgePermission(String name, String actions) {
        super(name, actions);
    }
}
