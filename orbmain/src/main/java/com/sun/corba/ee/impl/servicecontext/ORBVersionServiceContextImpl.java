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

import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.orb.ORBVersion ;
import com.sun.corba.ee.spi.orb.ORBVersionFactory ;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.servicecontext.ServiceContextBase ;
import com.sun.corba.ee.spi.servicecontext.ORBVersionServiceContext ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

public class ORBVersionServiceContextImpl extends ServiceContextBase
    implements ORBVersionServiceContext 
{
    // current ORB Version
    private ORBVersion version = ORBVersionFactory.getORBVersion() ;

    public static final ORBVersionServiceContext singleton =
        new ORBVersionServiceContextImpl() ;

    public ORBVersionServiceContextImpl( )
    {
        version = ORBVersionFactory.getORBVersion() ;
    }

    public ORBVersionServiceContextImpl( ORBVersion ver )
    {
        this.version = ver ;
    }

    public ORBVersionServiceContextImpl(InputStream is, GIOPVersion gv)
    {
        super(is) ;
        // pay particular attention to where the version is being read from!
        // is contains an encapsulation, ServiceContext reads off the
        // encapsulation and leaves the pointer in the variable "in",
        // which points to the long value.

        version = ORBVersionFactory.create( in ) ;
    }

    public int getId() 
    { 
        return SERVICE_CONTEXT_ID ; 
    }

    public void writeData( OutputStream os ) throws SystemException
    {
        version.write( os ) ;
    }

    public ORBVersion getVersion() 
    {
        return version ;
    }

    public String toString() 
    {
        return "ORBVersionServiceContextImpl[ version=" + version + " ]" ;
    }
}
