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

package com.sun.corba.ee.impl.oa.toa ;

import java.util.Map ;
import java.util.HashMap ;


import com.sun.corba.ee.spi.oa.ObjectAdapterFactory ;
import com.sun.corba.ee.spi.oa.ObjectAdapter ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.ior.ObjectAdapterId ;


import com.sun.corba.ee.impl.javax.rmi.CORBA.Util ;

import com.sun.corba.ee.impl.ior.ObjectKeyTemplateBase ;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

@ManagedObject
@Description( "The Factory for the TOA (transient object adapter)")
@AMXMetadata( isSingleton=true )
public class TOAFactory implements ObjectAdapterFactory 
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private ORB orb ;

    private TOAImpl toa ;
    private Map<String,TOAImpl> codebaseToTOA ;
    private TransientObjectManager tom ; 

    @ManagedAttribute
    @Description( "The default TOA used only for dispatch, not objref creation")
    private TOAImpl getDefaultTOA() {
        return toa ;
    }

    @ManagedAttribute
    @Description( "The map from Codebase to TOA")
    private synchronized Map<String,TOAImpl> getCodebaseMap() {
        return new HashMap<String,TOAImpl>( codebaseToTOA ) ;
    }

    public ObjectAdapter find ( ObjectAdapterId oaid ) 
    {
        if (oaid.equals( ObjectKeyTemplateBase.JIDL_OAID )  ) {
            return getTOA();
        } else {
            throw wrapper.badToaOaid();
        }
    }

    public void init( ORB orb )
    {
        this.orb = orb ;
        tom = new TransientObjectManager( orb ) ;
        codebaseToTOA = new HashMap<String,TOAImpl>() ;
        if (orb.mom() != null) {
            orb.mom().registerAtRoot( this ) ;
        }
    }

    public void shutdown( boolean waitForCompletion )
    {
        if (Util.getInstance() != null) {
            Util.getInstance().unregisterTargetsForORB(orb);
        }
    }

    public synchronized TOA getTOA( String codebase )
    {
        TOAImpl myToa = codebaseToTOA.get( codebase ) ;
        if (myToa == null) {
            myToa = new TOAImpl( orb, tom, codebase ) ;

            codebaseToTOA.put( codebase, myToa ) ;
        }

        return myToa ;
    }

    public synchronized TOA getTOA() 
    {
        if (toa == null) {
            // The dispatch-only TOA is not used for creating
            // objrefs, so its codebase can be null (and must
            // be, since we do not have a servant at this point)
            toa = new TOAImpl( orb, tom, null ) ;
        }

        return toa ;
    }

    public ORB getORB() 
    {
        return orb ;
    }
} ;

