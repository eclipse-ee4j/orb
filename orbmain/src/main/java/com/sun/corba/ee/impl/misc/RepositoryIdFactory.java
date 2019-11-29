/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.misc;

import com.sun.corba.ee.spi.orb.ORB;

/**
 * Holds a {@link RepIdDelegator}
 */
public abstract class RepositoryIdFactory
{
    private static final RepIdDelegator currentDelegator
        = new RepIdDelegator();

    /**
     * Returns the latest version RepositoryIdStrings instance
     * @return the delegate
     */
    public static RepositoryIdStrings getRepIdStringsFactory()
    {
        return currentDelegator;
    }

    /**
     * Checks the version of the ORB and returns the appropriate
     * RepositoryIdStrings instance.
     * @param orb ignored
     * @return the delegate
     */
    public static RepositoryIdStrings getRepIdStringsFactory(ORB orb)
    {
        return currentDelegator;
    }

    /**
     * Returns the latest version RepositoryIdUtility instance
     * @return the delegate
     */
    public static RepositoryIdUtility getRepIdUtility()
    {
        return currentDelegator;
    }

    /**
     * Checks the version of the ORB and returns the appropriate
     * RepositoryIdUtility instance.
     * @param orb ORB to get version
     * @return the delegate
     */
    public static RepositoryIdUtility getRepIdUtility(ORB orb)
    {
        return currentDelegator;
    }
}
