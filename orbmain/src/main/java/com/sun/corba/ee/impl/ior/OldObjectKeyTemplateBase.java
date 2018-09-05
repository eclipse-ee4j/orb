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

import com.sun.corba.ee.spi.ior.ObjectAdapterId ;

import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBVersion ;
import com.sun.corba.ee.spi.orb.ORBVersionFactory ;

import com.sun.corba.ee.impl.ior.ObjectKeyFactoryImpl ;

/**
 * @author Ken Cavanaugh
 */
public abstract class OldObjectKeyTemplateBase extends ObjectKeyTemplateBase 
{
    public OldObjectKeyTemplateBase( ORB orb, int magic, int scid, int serverid,
        String orbid, ObjectAdapterId oaid ) 
    {
        super( orb, magic, scid, serverid, orbid, oaid ) ;

        // set version based on magic
        if (magic == ObjectKeyFactoryImpl.JAVAMAGIC_OLD)
            setORBVersion( ORBVersionFactory.getOLD() ) ;
        else if (magic == ObjectKeyFactoryImpl.JAVAMAGIC_NEW)
            setORBVersion( ORBVersionFactory.getNEW() ) ;
        else // any other magic should not be here
            throw wrapper.badMagic( Integer.valueOf( magic ) ) ;
    }
}
