/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.misc;

import org.omg.CORBA.ORB;
import com.sun.corba.ee.impl.util.RepositoryId;

/**
 * Utility methods for working with repository IDs.
 */
public interface RepositoryIdUtility
{
    boolean isChunkedEncoding(int valueTag);
    boolean isCodeBasePresent(int valueTag);

    // These are currently the same in both RepositoryId and
    // RepositoryId_1_3, but provide the constants again here
    // to eliminate awkardness when using this interface.
    int NO_TYPE_INFO = RepositoryId.kNoTypeInfo;
    int SINGLE_REP_TYPE_INFO = RepositoryId.kSingleRepTypeInfo;
    int PARTIAL_LIST_TYPE_INFO = RepositoryId.kPartialListTypeInfo;

    // Determine how many (if any) repository IDs follow the value
    // tag.
    int getTypeInfo(int valueTag);

    // Accessors for precomputed value tags
    int getStandardRMIChunkedNoRepStrId();
    int getCodeBaseRMIChunkedNoRepStrId();
    int getStandardRMIChunkedId();
    int getCodeBaseRMIChunkedId();
    int getStandardRMIUnchunkedId();
    int getCodeBaseRMIUnchunkedId();
    int getStandardRMIUnchunkedNoRepStrId();
    int getCodeBaseRMIUnchunkedNoRepStrId();
}
