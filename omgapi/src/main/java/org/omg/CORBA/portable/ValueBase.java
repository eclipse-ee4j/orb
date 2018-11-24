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

package org.omg.CORBA.portable;

/**
 * The generated Java classes corresponding to valuetype IDL types implement this interface. In other words, the Java
 * mapping of valuetype objects implement the ValueBase interface. The generated Java class for valuetype's shall
 * provide an implementation of the ValueBase interface for the corresponding value type. For value types that are
 * streamable (i.e. non-custom), the generated Java class shall also provide an implementation for the
 * org.omg.CORBA.portable.Streamable interface. (CORBA::ValueBase is mapped to java.io.Serializable.)
 */
public interface ValueBase extends IDLEntity {
    /**
     * Provides truncatable repository ids.
     *
     * @return a String array--list of truncatable repository ids.
     */
    String[] _truncatable_ids();
}
