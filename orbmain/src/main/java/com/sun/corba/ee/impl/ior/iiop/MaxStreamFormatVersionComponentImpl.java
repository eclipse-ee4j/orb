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

package com.sun.corba.ee.impl.ior.iiop;

import org.omg.IOP.TAG_RMI_CUSTOM_MAX_STREAM_FORMAT;

import org.omg.CORBA_2_3.portable.OutputStream;

import javax.rmi.CORBA.ValueHandler;
import javax.rmi.CORBA.ValueHandlerMultiFormat;

import com.sun.corba.ee.impl.misc.ORBUtility;

import com.sun.corba.ee.spi.ior.TaggedComponentBase;

import com.sun.corba.ee.spi.ior.iiop.MaxStreamFormatVersionComponent;

// Java to IDL ptc 02-01-12 1.4.11
// TAG_RMI_CUSTOM_MAX_STREAM_FORMAT
public class MaxStreamFormatVersionComponentImpl extends TaggedComponentBase 
    implements MaxStreamFormatVersionComponent
{
    private byte version;

    public static final MaxStreamFormatVersionComponentImpl singleton
        = new MaxStreamFormatVersionComponentImpl();

    public boolean equals(Object obj)
    {
        if (!(obj instanceof MaxStreamFormatVersionComponentImpl))
            return false ;

        MaxStreamFormatVersionComponentImpl other = 
            (MaxStreamFormatVersionComponentImpl)obj ;

        return version == other.version ;
    }

    public int hashCode()
    {
        return version ;
    }

    public String toString()
    {
        return "MaxStreamFormatVersionComponentImpl[version=" + version + "]" ;
    }

    public MaxStreamFormatVersionComponentImpl()
    {
        version = ORBUtility.getMaxStreamFormatVersion();
    }

    public MaxStreamFormatVersionComponentImpl(byte streamFormatVersion) {
        version = streamFormatVersion;
    }

    public byte getMaxStreamFormatVersion()
    {
        return version;
    }

    public void writeContents(OutputStream os) 
    {
        os.write_octet(version);
    }
    
    public int getId() 
    {
        return TAG_RMI_CUSTOM_MAX_STREAM_FORMAT.value;
    }
}
