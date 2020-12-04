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

package org.omg.CORBA.portable;

/**
 * Java to IDL ptc 02-01-12 1.5.1.3
 *
 * ValueOutputStream is used for implementing RMI-IIOP
 * stream format version 2.
 */
public interface ValueOutputStream {
    /**
     * The start_value method ends any currently open chunk,
     * writes a valuetype header for a nested custom valuetype
     * (with a null codebase and the specified repository ID),
     * and increments the valuetype nesting depth.
     * @param rep_id the repository id
     */
    void start_value(java.lang.String rep_id);

    /**
     * The end_value method ends any currently open chunk,
     * writes the end tag for the nested custom valuetype,
     * and decrements the valuetype nesting depth.
     */
    void end_value();
}
