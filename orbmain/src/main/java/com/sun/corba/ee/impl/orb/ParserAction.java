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

package com.sun.corba.ee.impl.orb;

import java.util.Properties;

public interface ParserAction {
    /**
     * Return the property name or prefix for which this action is applied.
     * 
     * @return the property name or prefix.
     */
    String getPropertyName();

    /**
     * Return whether this action is for an exact match or a prefix match (true).
     * 
     * @return true if this action is for an exact or prefix match.
     */
    boolean isPrefix();

    /**
     * Return the field name in an object that is set with the result
     * 
     * @return the field name
     */
    String getFieldName();

    /**
     * Apply this action to props and return the result.
     * 
     * @param props properties to apply action to
     * @return result of action
     */
    Object apply(Properties props);
}
