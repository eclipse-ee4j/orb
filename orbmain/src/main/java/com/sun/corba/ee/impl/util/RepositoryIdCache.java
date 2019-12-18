/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.util;

import java.util.Hashtable;

public class RepositoryIdCache extends Hashtable<String, RepositoryId> {
    public final synchronized RepositoryId getId(String key) {
        RepositoryId repId = super.get(key);

        if (repId != null) {
            return repId;
        } else {
            repId = new RepositoryId(key);
            put(key, repId);
            return repId;
        }
    }
}
