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

import java.util.Stack;
import java.util.Hashtable;
import java.util.EmptyStackException;
import java.util.Enumeration;

public class RepositoryIdCache extends Hashtable {
    public final synchronized RepositoryId getId(String key) {
        RepositoryId repId = (RepositoryId) super.get(key);

        if (repId != null) {
            return repId;
        } else {
            repId = new RepositoryId(key);
            put(key, repId);
            return repId;
        }
    }
}
