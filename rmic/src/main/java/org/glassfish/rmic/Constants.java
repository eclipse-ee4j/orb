/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic;

import org.glassfish.rmic.tools.java.Identifier;

/**
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
public interface Constants extends org.glassfish.rmic.tools.java.Constants {

    /*
     * Identifiers potentially useful for all Generators
     */
    public static final Identifier idRemote = Identifier.lookup("java.rmi.Remote");
    public static final Identifier idRemoteException = Identifier.lookup("java.rmi.RemoteException");
}
