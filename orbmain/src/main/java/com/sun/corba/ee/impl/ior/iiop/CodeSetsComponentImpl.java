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

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.ior.TaggedComponentBase ;

import com.sun.corba.ee.spi.ior.iiop.CodeSetsComponent ;

import org.omg.IOP.TAG_CODE_SETS ;

import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo ;
import com.sun.corba.ee.impl.encoding.MarshalOutputStream ;
import com.sun.corba.ee.impl.encoding.MarshalInputStream ;

public class CodeSetsComponentImpl extends TaggedComponentBase 
    implements CodeSetsComponent
{
    CodeSetComponentInfo csci ;
 
    public boolean equals( Object obj )
    {
        if (!(obj instanceof CodeSetsComponentImpl)) 
            return false ;

        CodeSetsComponentImpl other = (CodeSetsComponentImpl)obj ;

        return csci.equals( other.csci ) ;
    }

    public int hashCode()
    {
        return csci.hashCode() ;
    }

    public String toString()
    {   
        return "CodeSetsComponentImpl[csci=" + csci + "]" ;
    }

    public CodeSetsComponentImpl() 
    {
        // Uses our default code sets (see CodeSetComponentInfo)
        csci = new CodeSetComponentInfo() ;
    }

    public CodeSetsComponentImpl( InputStream is )
    {
        csci = new CodeSetComponentInfo() ;
        csci.read( (MarshalInputStream)is ) ;
    }

    public CodeSetsComponentImpl(com.sun.corba.ee.spi.orb.ORB orb)
    {
        if (orb == null)
            csci = new CodeSetComponentInfo();
        else
            csci = orb.getORBData().getCodeSetComponentInfo();
    }
    
    public CodeSetComponentInfo getCodeSetComponentInfo()
    {
        return csci ;
    }

    public void writeContents(OutputStream os) 
    {
        csci.write( (MarshalOutputStream)os ) ;
    }
    
    public int getId() 
    {
        return TAG_CODE_SETS.value ; // 1 in CORBA 2.3.1 13.6.3
    }
}
