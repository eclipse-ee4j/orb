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

import org.omg.CORBA_2_3.portable.InputStream;

import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate;

/**
 * Construct ObjectKey and ObjectKeyTemplate instances from their CDR-marshalled representation.
 */
public interface ObjectKeyFactory {
    /**
     * Create an ObjectKey from its octet sequence representation.
     */
    ObjectKey create(byte[] key);

    /**
     * Create an ObjectKeyTemplate from its representation in an InputStream.
     */
    ObjectKeyTemplate createTemplate(InputStream is);
}
