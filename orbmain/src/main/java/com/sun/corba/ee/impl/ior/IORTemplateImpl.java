/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior;

import java.util.Iterator ;

import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.ee.spi.ior.IdentifiableContainerBase ;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.ee.spi.ior.IORTemplate ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.IORFactory ;

import com.sun.corba.ee.spi.orb.ORB ;

/**
 * This class is a container of TaggedProfileTemplates.
 * @author 
 */
public class IORTemplateImpl 
    extends IdentifiableContainerBase<TaggedProfileTemplate>
    implements IORTemplate
{
    private ObjectKeyTemplate oktemp ;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( "IORTemplate[oktemp=") ;
        sb.append( oktemp.toString() ) ;
        sb.append( " profile templates:") ;
        sb.append( super.toString() ) ;
        sb.append( ']' ) ;
        return sb.toString() ;
    }

    @Override
    public boolean equals( Object obj )
    {
        if (obj == null)
            return false ;

        if (!(obj instanceof IORTemplateImpl))
            return false ;

        IORTemplateImpl other = (IORTemplateImpl)obj ;

        return super.equals( obj ) && oktemp.equals( other.getObjectKeyTemplate() ) ;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode() ^ oktemp.hashCode() ;
    }

    public ObjectKeyTemplate getObjectKeyTemplate()
    {
        return oktemp ;
    }

    public IORTemplateImpl( ObjectKeyTemplate oktemp )
    {
        this.oktemp = oktemp ;
    }

    public IOR makeIOR( ORB orb, String typeid, ObjectId oid ) 
    {
        return new IORImpl( orb, typeid, this, oid ) ;
    }

    public boolean isEquivalent( IORFactory other ) 
    {
        if (!(other instanceof IORTemplate))
            return false ;

        IORTemplate list = (IORTemplate)other ;

        Iterator<TaggedProfileTemplate> thisIterator = iterator() ;
        Iterator<TaggedProfileTemplate> listIterator = list.iterator() ;
        while (thisIterator.hasNext() && listIterator.hasNext()) {
            TaggedProfileTemplate thisTemplate = thisIterator.next() ;
            TaggedProfileTemplate listTemplate = listIterator.next() ;
            if (!thisTemplate.isEquivalent( listTemplate ))
                return false ;
        }

        return (thisIterator.hasNext() == listIterator.hasNext()) &&
            getObjectKeyTemplate().equals( list.getObjectKeyTemplate() ) ;
    }

    /** Ensure that this IORTemplate and all of its profiles can not be
    * modified.  This overrides the method inherited from 
    * FreezableList through IdentifiableContainerBase.
    */
    @Override
    public void makeImmutable()
    {
        makeElementsImmutable() ;
        super.makeImmutable() ;
    }

    public void write( OutputStream os ) 
    {
        oktemp.write( os ) ;
        EncapsulationUtility.writeIdentifiableSequence( this, os ) ;
    }

    public IORTemplateImpl( InputStream is ) 
    {
        ORB orb = (ORB)(is.orb()) ;
        IdentifiableFactoryFinder<TaggedProfileTemplate> finder = 
            orb.getTaggedProfileTemplateFactoryFinder() ;

        oktemp = orb.getObjectKeyFactory().createTemplate( is ) ;
        EncapsulationUtility.readIdentifiableSequence( this, finder, is ) ;

        makeImmutable() ;
    }
}
