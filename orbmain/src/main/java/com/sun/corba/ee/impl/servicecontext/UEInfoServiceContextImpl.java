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

import java.io.Serializable;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.servicecontext.ServiceContextBase;
import com.sun.corba.ee.spi.servicecontext.UEInfoServiceContext;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

public class UEInfoServiceContextImpl extends ServiceContextBase implements UEInfoServiceContext {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private Throwable unknown = null;

    public UEInfoServiceContextImpl(Throwable ex) {
        unknown = ex;
    }

    public UEInfoServiceContextImpl(InputStream is, GIOPVersion gv) {
        super(is);

        try {
            unknown = (Throwable) in.read_value();
        } catch (Exception e) {
            unknown = wrapper.couldNotReadInfo(e);
        }
    }

    public int getId() {
        return SERVICE_CONTEXT_ID;
    }

    public void writeData(OutputStream os) {
        os.write_value((Serializable) unknown);
    }

    public Throwable getUE() {
        return unknown;
    }

    @Override
    public String toString() {
        return "UEInfoServiceContextImpl[ unknown=" + unknown.toString() + " ]";
    }
}
