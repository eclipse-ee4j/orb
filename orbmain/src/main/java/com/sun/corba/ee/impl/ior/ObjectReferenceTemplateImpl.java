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

package com.sun.corba.ee.impl.ior ;

import org.omg.CORBA.portable.InputStream ;
import org.omg.CORBA.portable.OutputStream ;
import org.omg.CORBA.portable.StreamableValue ;

import org.omg.CORBA.TypeCode ;

import org.omg.PortableInterceptor.ObjectReferenceTemplate ;
import org.omg.PortableInterceptor.ObjectReferenceTemplateHelper ;


import com.sun.corba.ee.spi.ior.ObjectAdapterId ;
import com.sun.corba.ee.spi.ior.IORFactory;
import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.ior.IORTemplateList;
import com.sun.corba.ee.spi.ior.IORFactories;


import com.sun.corba.ee.spi.orb.ORB ;

/** This is an implementation of the ObjectReferenceTemplate abstract value 
* type defined by the portable interceptors IDL.  
* Note that this is a direct Java implementation
* of the abstract value type: there is no stateful value type defined in IDL,
* since defining the state in IDL is awkward and inefficient.  The best way
* to define the state is to use internal data structures that can be written
* to and read from CORBA streams.
*/
public class ObjectReferenceTemplateImpl extends ObjectReferenceProducerBase
    implements ObjectReferenceTemplate, StreamableValue 
{
    private static final long serialVersionUID = 6441570404699638098L;
    transient private IORTemplate iorTemplate ;

    public ObjectReferenceTemplateImpl( InputStream is )
    {
        super( (ORB)(is.orb()) ) ;
        _read( is ) ;
    }

    public ObjectReferenceTemplateImpl( ORB orb, IORTemplate iortemp ) 
    {
        super( orb ) ;
        iorTemplate = iortemp ;
    }

    @Override
    public boolean equals( Object obj )
    {
        if (!(obj instanceof ObjectReferenceTemplateImpl)) {
            return false ;
        }

        ObjectReferenceTemplateImpl other = (ObjectReferenceTemplateImpl)obj ;

        return (iorTemplate != null) && 
            iorTemplate.equals( other.iorTemplate ) ;
    }

    @Override
    public int hashCode()
    {
        return iorTemplate.hashCode() ;
    }

    // Note that this repository ID must reflect the implementation
    // of the abstract valuetype (that is, this class), not the
    // repository ID of the org.omg.PortableInterceptor.ObjectReferenceTemplate
    // class.  This allows for multiple independent implementations 
    // of the abstract valuetype, should that become necessary.
    public static final String repositoryId = 
        "IDL:com/sun/corba/ee/impl/ior/ObjectReferenceTemplateImpl:1.0" ;

    public String[] _truncatable_ids() 
    {
        return new String[] { repositoryId } ;
    }

    public TypeCode _type() 
    {
        return ObjectReferenceTemplateHelper.type() ;
    }

    // Read the data into a (presumably) empty ORTImpl.  This sets the
    // orb to the ORB of the InputStream.
    public void _read( InputStream is ) 
    {
        org.omg.CORBA_2_3.portable.InputStream istr =
            (org.omg.CORBA_2_3.portable.InputStream)is ;
        iorTemplate = IORFactories.makeIORTemplate( istr ) ;
        orb = (ORB)(istr.orb()) ;
    }

    public void _write( OutputStream os ) 
    {
        org.omg.CORBA_2_3.portable.OutputStream ostr = 
            (org.omg.CORBA_2_3.portable.OutputStream)os ;

        iorTemplate.write( ostr ) ;
    }

    public String server_id ()
    {
        int val = iorTemplate.getObjectKeyTemplate().getServerId() ;
        return Integer.toString( val ) ;
    }

    public String orb_id ()
    {
        return iorTemplate.getObjectKeyTemplate().getORBId() ;
    }

    public String[] adapter_name()
    {
        ObjectAdapterId poaid = 
            iorTemplate.getObjectKeyTemplate().getObjectAdapterId() ;

        return poaid.getAdapterName() ;
    }

    public IORFactory getIORFactory() 
    {
        return iorTemplate ;
    }

    public IORTemplateList getIORTemplateList() 
    {
        IORTemplateList tl = IORFactories.makeIORTemplateList() ;
        tl.add( iorTemplate ) ;
        tl.makeImmutable() ;
        return tl ;
    }
}
