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
 * Java to IDL ptc 02-01-12 1.5.1.4
 *
 * ValueInputStream is used for implementing RMI-IIOP
 * stream format version 2.
 */
public interface ValueInputStream {

    /**
     * The start_value method reads a valuetype
     * header for a nested custom valuetype and
     * increments the valuetype nesting depth.
     */
    void start_value();

    /**
     * The end_value method reads the end tag
     * for the nested custom valuetype (after
     * skipping any data that precedes the end
     * tag) and decrements the valuetype nesting
     * depth.
     */
    void end_value();
}

