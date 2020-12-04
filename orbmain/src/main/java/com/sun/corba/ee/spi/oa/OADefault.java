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
