/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package com.sun.corba.ee.impl.protocol;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher;

/**
 * INSServerRequestDispatcher handles all INS related discovery request. The INS Service
 * can be registered using ORB.register_initial_reference().
 * This Singleton subcontract just 
 * finds the target IOR and does location forward.
 * XXX PI points are not invoked in either dispatch() or locate() method this
 * should be fixed in Tiger.
 */ 
public class INSServerRequestDispatcher 
    implements ServerRequestDispatcher
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private ORB orb = null;

    public INSServerRequestDispatcher( ORB orb ) {
        this.orb = orb;
    }

    // Need to signal one of OBJECT_HERE, OBJECT_FORWARD, OBJECT_NOT_EXIST.
    public IOR locate(ObjectKey okey) { 
        // send a locate forward with the right IOR. If the insKey is not 
        // registered then it will throw OBJECT_NOT_EXIST Exception
        String insKey = new String( okey.getBytes(orb) );
        return getINSReference( insKey );
    }

    public void dispatch(MessageMediator request)
    {
        // send a locate forward with the right IOR. If the insKey is not 
        // registered then it will throw OBJECT_NOT_EXIST Exception
        String insKey = new String( 
            request.getObjectKeyCacheEntry().getObjectKey().getBytes(orb) );
        request.getProtocolHandler()
            .createLocationForward(request, getINSReference( insKey ), null);
        return;
    }

    /**
     * getINSReference if it is registered in INSObjectKeyMap.
     */
    private IOR getINSReference( String insKey ) {
        IOR entry = orb.getIOR( orb.getLocalResolver().resolve( insKey ), false ) ;
        if( entry != null ) {
            // If entry is not null then the locate is with an INS Object key,
            // so send a location forward with the right IOR.
            return entry;
        }

        throw wrapper.servantNotFound() ;
    }
}
