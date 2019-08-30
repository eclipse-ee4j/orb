/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.oa.rfm ;

public interface ReferenceFactory extends org.omg.CORBA.Object,
    org.omg.CORBA.portable.IDLEntity 
{
    /** Create an object reference with the given key and
     * repository ID.
     * 
     * @param key key for object
     * @return the resulting object
     */
    org.omg.CORBA.Object createReference( byte[] key ) ;

    /** Destroy this ReferenceFactory.
     */
    void destroy() ;
}
