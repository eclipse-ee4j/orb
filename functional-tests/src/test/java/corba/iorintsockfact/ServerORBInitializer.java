/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2002 Jul 19 (Fri) 13:42:23 by Harold Carr.
// Last Modified : 2003 Jun 03 (Tue) 18:06:21 by Harold Carr.
//

package corba.iorintsockfact;

import org.omg.CosNaming.*;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.legacy.interceptor.ORBInitInfoExt ;

/**
 * @author Harold Carr
 */
public class ServerORBInitializer
    extends
        org.omg.CORBA.LocalObject
    implements
        ORBInitializer
{
    public static final String baseMsg = ServerORBInitializer.class.getName();

    public void pre_init(ORBInitInfo info) { }

    public void post_init(ORBInitInfo info)
    {
        ORB orb = ((ORBInitInfoExt)info).getORB() ;
        try {
            info.add_ior_interceptor(new IORInterceptor(orb));
        } catch (DuplicateName ex) {
            System.out.println(baseMsg + ex);
            System.exit(-1);
        }
    }
}

// End of file.
