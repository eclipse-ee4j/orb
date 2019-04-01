/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.servicecontext;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.servicecontext.ServiceContextBase;
import com.sun.corba.ee.spi.servicecontext.MaxStreamFormatVersionServiceContext;

import com.sun.corba.ee.impl.misc.ORBUtility;

public class MaxStreamFormatVersionServiceContextImpl extends ServiceContextBase implements MaxStreamFormatVersionServiceContext {
    private byte maxStreamFormatVersion;

    // The singleton uses the maximum version indicated by our
    // ValueHandler.
    public static final MaxStreamFormatVersionServiceContext singleton = new MaxStreamFormatVersionServiceContextImpl();

    private MaxStreamFormatVersionServiceContextImpl() {
        maxStreamFormatVersion = ORBUtility.getMaxStreamFormatVersion();
    }

    public MaxStreamFormatVersionServiceContextImpl(byte maxStreamFormatVersion) {
        this.maxStreamFormatVersion = maxStreamFormatVersion;
    }

    public MaxStreamFormatVersionServiceContextImpl(InputStream is, GIOPVersion gv) {
        super(is);

        maxStreamFormatVersion = is.read_octet();
    }

    public int getId() {
        return SERVICE_CONTEXT_ID;
    }

    public void writeData(OutputStream os) {
        os.write_octet(maxStreamFormatVersion);
    }

    public byte getMaximumStreamFormatVersion() {
        return maxStreamFormatVersion;
    }

    public String toString() {
        return "MaxStreamFormatVersionServiceContextImpl[" + maxStreamFormatVersion + "]";
    }
}
