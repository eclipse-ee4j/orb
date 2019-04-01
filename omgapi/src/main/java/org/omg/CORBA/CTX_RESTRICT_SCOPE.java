/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA;

/**
 * A flag that can be used as the second parameter to the method <code>Context.get_values</code> to restrict the search
 * scope. When this flag is used, it restricts the search for context values to this particular <code>Context</code>
 * object or to the scope specified in the first parameter to <code>Context.get_values</code>.
 * <P>
 * Usage:
 *
 * <PRE>
 * NVList props = myContext.get_values("_USER", CTX_RESTRICT_SCOPE.value, "id*");
 * </PRE>
 *
 * @see org.omg.CORBA.Context#get_values(String, int, String)
 * @version 1.3, 09/09/97
 * @since JDK1.2
 */
public interface CTX_RESTRICT_SCOPE {

    /**
     * The field containing the <code>int</code> value of a <code>CTX_RESTRICT_SCOPE</code> flag.
     */
    int value = 15;
}
