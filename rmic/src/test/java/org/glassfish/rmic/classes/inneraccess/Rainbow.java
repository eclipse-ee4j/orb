/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.classes.inneraccess;

import java.rmi.RemoteException;

public class Rainbow {

    class CheckerImpl implements ColorChecker {
        @Override
        public String getPreferredColor() throws RemoteException {
            return "blue";
        }
    }

    public static String getQualifiedCheckerClassName() {
        return Rainbow.class.getName() + ".CheckerImpl";
    }

    public static Class<?> getInterfaceCheckerClass() {
        return CheckerImpl.class;
    }
}
