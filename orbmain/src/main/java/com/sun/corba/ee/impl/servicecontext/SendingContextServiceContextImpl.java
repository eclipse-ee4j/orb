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

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.IORFactories ;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

import com.sun.corba.ee.spi.servicecontext.ServiceContextBase ;
import com.sun.corba.ee.spi.servicecontext.SendingContextServiceContext ;

public class SendingContextServiceContextImpl extends ServiceContextBase
    implements SendingContextServiceContext
{
    private IOR ior = null ;

    public SendingContextServiceContextImpl( IOR ior )
    {
        this.ior = ior ;
    }

    public SendingContextServiceContextImpl(InputStream is, GIOPVersion gv)
    {
        super(is) ;
        ior = IORFactories.makeIOR( (ORB)is.orb(), is ) ;       
    }

    public int getId() 
    { 
        return SERVICE_CONTEXT_ID ; 
    }

    public void writeData( OutputStream os ) 
    {
        ior.write( os ) ;
    }

    public IOR getIOR() 
    {
        return ior ;
    }

    public String toString() 
    {
        return "SendingContexServiceContextImpl[ ior=" + ior + " ]" ;
    }
}
