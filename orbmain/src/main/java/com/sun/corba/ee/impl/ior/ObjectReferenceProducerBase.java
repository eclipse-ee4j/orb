/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.ior.IORFactory ;
import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.IORFactories ;
import com.sun.corba.ee.spi.ior.IORTemplateList ;
import com.sun.corba.ee.spi.ior.ObjectId ;

import com.sun.corba.ee.impl.misc.ORBUtility ;
import java.io.Serializable;


// Made this serializable so that derived class ObjectReferenceFactoryImpl
// does not require a void constructor.  Instead, this class is Serializable,
// and Object is its superclass, so Object provides the void constructor.
// This change cleans up a findbugs issue.
public abstract class ObjectReferenceProducerBase implements Serializable {
    private static final long serialVersionUID = 6478965304620421549L;
    transient protected ORB orb ;

    public abstract IORFactory getIORFactory() ;

    public abstract IORTemplateList getIORTemplateList() ;

    public ObjectReferenceProducerBase( ORB orb ) 
    {
        this.orb = orb ;
    }

    public org.omg.CORBA.Object make_object (String repositoryId, 
        byte[] objectId)
    {
        ObjectId oid = IORFactories.makeObjectId( objectId ) ;
        IOR ior = getIORFactory().makeIOR( orb, repositoryId, oid ) ;

        return ORBUtility.makeObjectReference( ior ) ;
    }
}

