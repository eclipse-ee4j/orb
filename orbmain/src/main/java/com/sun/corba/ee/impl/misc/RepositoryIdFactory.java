/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
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

package com.sun.corba.ee.impl.misc;

import com.sun.corba.ee.spi.orb.ORB;

/**
 * Holds a {@link RepIdDelegator}
 */
public abstract class RepositoryIdFactory {
    private static final RepIdDelegator currentDelegator = new RepIdDelegator();

    /**
     * Returns the latest version RepositoryIdStrings instance
     * 
     * @return the delegate
     */
    public static RepositoryIdStrings getRepIdStringsFactory() {
        return currentDelegator;
    }

    /**
     * Checks the version of the ORB and returns the appropriate RepositoryIdStrings instance.
     * 
     * @param orb ignored
     * @return the delegate
     */
    public static RepositoryIdStrings getRepIdStringsFactory(ORB orb) {
        return currentDelegator;
    }

    /**
     * Returns the latest version RepositoryIdUtility instance
     * 
     * @return the delegate
     */
    public static RepositoryIdUtility getRepIdUtility() {
        return currentDelegator;
    }

    /**
     * Checks the version of the ORB and returns the appropriate RepositoryIdUtility instance.
     * 
     * @param orb ORB to get version
     * @return the delegate
     */
    public static RepositoryIdUtility getRepIdUtility(ORB orb) {
        return currentDelegator;
    }
}
