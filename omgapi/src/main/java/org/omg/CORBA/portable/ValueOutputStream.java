/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
