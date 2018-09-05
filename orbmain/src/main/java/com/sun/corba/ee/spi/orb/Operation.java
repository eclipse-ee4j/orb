/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.orb ;

/** A generic class representing a function that takes a value and returns
 * a value.  This is a building block for property parsing.
 */
public interface Operation{
    /** Apply some function to a value and return the result.
    */
    Object operate( Object value ) ;
}
