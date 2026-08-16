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
