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

package com.sun.corba.ee.spi.orb ;

import com.sun.corba.ee.spi.orb.ORBVersion ;
import com.sun.corba.ee.impl.orb.ORBVersionImpl ;
import org.omg.CORBA.portable.InputStream ;
import org.omg.CORBA.INTERNAL ;

public class ORBVersionFactory {
    private ORBVersionFactory() {} ;

    public static ORBVersion getFOREIGN() 
    {
        return ORBVersionImpl.FOREIGN ;
    }

    public static ORBVersion getOLD() 
    {
        return ORBVersionImpl.OLD ;
    }

    public static ORBVersion getNEW() 
    {
        return ORBVersionImpl.NEW ;
    }

    public static ORBVersion getJDK1_3_1_01() 
    {
        return ORBVersionImpl.JDK1_3_1_01 ;
    }

    public static ORBVersion getNEWER() 
    {
        return ORBVersionImpl.NEWER ;
    }

    public static ORBVersion getPEORB() 
    {
        return ORBVersionImpl.PEORB ;
    }

    /** Return the current version of this ORB
     * @return ORB version
     */
    public static ORBVersion getORBVersion()
    {
        return ORBVersionImpl.PEORB ;
    }

    public static ORBVersion create( InputStream is ) 
    {
        byte value = is.read_octet() ;
        return byteToVersion( value ) ;
    }

    private static ORBVersion byteToVersion( byte value ) 
    {
        /* Throwing an exception here would cause this version to be 
        * incompatible with future versions of the ORB, to the point 
        * that this version could
        * not even unmarshal objrefs from a newer version that uses 
        * extended versioning.  Therefore, we will simply treat all 
        * unknown versions as the latest version.
        if (value < 0)
            throw new INTERNAL() ;
        */

        /**
         * Update: If we treat all unknown versions as the latest version
         * then when we send an IOR with a PEORB version to an ORB that
         * doesn't know the PEORB version it will treat it as whatever
         * its idea of the latest version is.  Then, if that IOR is
         * sent back to the server and compared with the original
         * the equality check will fail because the versions will be
         * different.
         *
         * Instead, just capture the version bytes.
         */

        switch (value) {
            case ORBVersion.FOREIGN : return ORBVersionImpl.FOREIGN ;
            case ORBVersion.OLD : return ORBVersionImpl.OLD ;
            case ORBVersion.NEW : return ORBVersionImpl.NEW ;
            case ORBVersion.JDK1_3_1_01: return ORBVersionImpl.JDK1_3_1_01 ;
            case ORBVersion.NEWER : return ORBVersionImpl.NEWER ;
            case ORBVersion.PEORB : return ORBVersionImpl.PEORB ;
            default : return new ORBVersionImpl(value); 
        }
    }
}
