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
import com.sun.corba.ee.spi.servicecontext.CodeSetServiceContext;

import com.sun.corba.ee.impl.encoding.MarshalInputStream ;
import com.sun.corba.ee.impl.encoding.MarshalOutputStream ;
import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo  ;

public class CodeSetServiceContextImpl extends ServiceContextBase
    implements CodeSetServiceContext 
{
    private CodeSetComponentInfo.CodeSetContext csc ;

    public CodeSetServiceContextImpl( CodeSetComponentInfo.CodeSetContext csc )
    {
        this.csc = csc ;
    }

    public CodeSetServiceContextImpl(InputStream is, GIOPVersion gv)
    {
        super(is) ;
        csc = new CodeSetComponentInfo.CodeSetContext() ;
        csc.read( (MarshalInputStream)in ) ;
    }

    public int getId() 
    { 
        return SERVICE_CONTEXT_ID ; 
    }

    public void writeData( OutputStream os ) 
    {
        csc.write( (MarshalOutputStream)os ) ;
    }
    
    public CodeSetComponentInfo.CodeSetContext getCodeSetContext() 
    {
        return csc ;
    }

    public String toString() 
    {
        return "CodeSetServiceContextImpl[ csc=" + csc + " ]" ;
    }
}
