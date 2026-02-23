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

import com.sun.corba.ee.spi.ior.TaggedComponentBase ;
import com.sun.corba.ee.spi.ior.iiop.ORBTypeComponent ;

import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.IOP.TAG_ORB_TYPE ;

/**
 * @author Ken Cavanaugh
 */
public class ORBTypeComponentImpl extends TaggedComponentBase 
    implements ORBTypeComponent
{
    private int ORBType;
   
    public boolean equals( Object obj )
    {
        if (!(obj instanceof ORBTypeComponentImpl))
            return false ;

        ORBTypeComponentImpl other = (ORBTypeComponentImpl)obj ;

        return ORBType == other.ORBType ;
    }

    public int hashCode()
    {
        return ORBType ;
    }

    public String toString()
    {
        return "ORBTypeComponentImpl[ORBType=" + ORBType + "]" ;
    }

    public ORBTypeComponentImpl(int ORBType) 
    {
        this.ORBType = ORBType ;
    }
    
    public int getId() 
    {
        return TAG_ORB_TYPE.value ; // 0 in CORBA 2.3.1 13.6.3
    }
    
    public int getORBType() 
    {
        return ORBType ;
    }
    
    public void writeContents(OutputStream os) 
    {
        os.write_ulong( ORBType ) ;
    }
}
