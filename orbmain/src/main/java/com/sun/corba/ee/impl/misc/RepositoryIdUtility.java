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

package com.sun.corba.ee.impl.misc;

import com.sun.corba.ee.impl.util.RepositoryId;

/**
 * Utility methods for working with repository IDs.
 */
public interface RepositoryIdUtility {
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
