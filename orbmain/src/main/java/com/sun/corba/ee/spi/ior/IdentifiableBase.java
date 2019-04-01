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

import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.Writeable;
import com.sun.corba.ee.spi.ior.WriteContents;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.ior.EncapsulationUtility;

/**
 * Provide support for properly reading and writing Identifiable objects that are also encapsulations (tagged profiles
 * and components).
 */
public abstract class IdentifiableBase implements Identifiable, WriteContents {
    /**
     * Write the data for this object as a CDR encapsulation. This is used for writing tagged components and profiles. These
     * data types must be written out as encapsulations, which means that we need to first write the data out to an
     * encapsulation stream, then extract the data and write it to os as an array of octets.
     */
    final public void write(OutputStream os) {
        EncapsulationUtility.writeEncapsulation((WriteContents) this, os);
    }
}
