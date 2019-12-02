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

package com.sun.corba.ee.impl.orb ;

import java.util.Properties ;

public interface ParserAction {
    /** Return the property name or prefix for which this action
     * is applied.
     * @return the property name or prefix.
     */
    String getPropertyName();

    /** Return whether this action is for an exact match or a prefix
     * match (true).
     * @return true if this action is for an exact or prefix match.
     */
    boolean isPrefix();

    /** Return the field name in an object that is set with the result
     * @return the field name
     */
    String getFieldName();

    /** Apply this action to props and return the result.
     * @param props properties to apply action to
     * @return result of action
    */
    Object apply( Properties props );
}
