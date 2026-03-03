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

package com.sun.corba.ee.impl.servicecontext;

import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.servicecontext.MaxStreamFormatVersionServiceContext ;
import com.sun.corba.ee.spi.servicecontext.ServiceContextBase ;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

public class MaxStreamFormatVersionServiceContextImpl extends ServiceContextBase 
    implements MaxStreamFormatVersionServiceContext
{
    private byte maxStreamFormatVersion;

    // The singleton uses the maximum version indicated by our
    // ValueHandler.
    public static final MaxStreamFormatVersionServiceContext singleton
        = new MaxStreamFormatVersionServiceContextImpl();

    private MaxStreamFormatVersionServiceContextImpl() 
    {
        maxStreamFormatVersion = ORBUtility.getMaxStreamFormatVersion();
    }

    public MaxStreamFormatVersionServiceContextImpl(byte maxStreamFormatVersion) 
    {
        this.maxStreamFormatVersion = maxStreamFormatVersion;
    }

    public MaxStreamFormatVersionServiceContextImpl(InputStream is, GIOPVersion gv) 
    {
        super(is) ;

        maxStreamFormatVersion = is.read_octet();
    }

    public int getId() 
    { 
        return SERVICE_CONTEXT_ID; 
    }

    public void writeData(OutputStream os) 
    {
        os.write_octet(maxStreamFormatVersion);
    }
    
    public byte getMaximumStreamFormatVersion()
    {
        return maxStreamFormatVersion;
    }

    public String toString() 
    {
        return "MaxStreamFormatVersionServiceContextImpl[" 
            + maxStreamFormatVersion + "]";
    }
}
    



