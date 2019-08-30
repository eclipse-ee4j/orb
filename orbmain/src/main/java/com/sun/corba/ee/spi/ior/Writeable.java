/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.ior;

import org.omg.CORBA_2_3.portable.OutputStream ;

/** This interface represents an entity that can be written to an OutputStream.
 * @author Ken Cavanaugh
 */
public interface Writeable 
{
    /** Write this object directly to the output stream.
     * 
     * @param arg0 object to write
     */
    void write(OutputStream arg0);
}
