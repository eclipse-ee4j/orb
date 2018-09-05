/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.oa ;

import com.sun.corba.ee.impl.oa.poa.POAFactory ;
import com.sun.corba.ee.impl.oa.toa.TOAFactory ;
import com.sun.corba.ee.spi.orb.ORB ;

/** OADefault provides methods to create the standard ObjectAdapterFactory
 * instances for this version of the ORB.  These methods are generally
 * used in ORBConfigurator instances to construct an ORB instance.
 */
public class OADefault {
    public static ObjectAdapterFactory makePOAFactory( ORB orb )
    {
        ObjectAdapterFactory oaf = new POAFactory() ;
        oaf.init( orb ) ;
        return oaf ;
    }

    public static ObjectAdapterFactory makeTOAFactory( ORB orb )
    {
        ObjectAdapterFactory oaf = new TOAFactory() ;
        oaf.init( orb ) ;
        return oaf ;
    }
}
