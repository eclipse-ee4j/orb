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
// Created       : 2001 May 23 (Wed) 20:32:27 by Harold Carr.
// Last Modified : 2001 Oct 02 (Tue) 20:33:57 by Harold Carr.
//

package pi.serviceexample;

import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;
import org.omg.PortableInterceptor.ORBInitInfo;


public class AServiceORBInitializer 
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ORBInitializer
{
    private AServiceImpl aServiceImpl;
    private AServiceInterceptor aServiceInterceptor;

    public void pre_init(ORBInitInfo info)
    {
        try {
            int id = info.allocate_slot_id();

            aServiceInterceptor = new AServiceInterceptor(id);

            info.add_client_request_interceptor(aServiceInterceptor);
            info.add_server_request_interceptor(aServiceInterceptor);

            // Create and register a reference to the service to be
            // used by client code.

            aServiceImpl = new AServiceImpl(id);

            info.register_initial_reference("AService", aServiceImpl);

        } catch (Throwable t) {
            System.out.println("Exception handling not shown.");
        }
    }

    public void post_init(ORBInitInfo info)
    {
        try {

            Current piCurrent =
                CurrentHelper.narrow(
                    info.resolve_initial_references("PICurrent"));
            aServiceImpl.setPICurrent(piCurrent);

            CodecFactory codecFactory =
                CodecFactoryHelper.narrow(
                    info.resolve_initial_references("CodecFactory"));
            Encoding encoding = new Encoding((short)0, (byte)1, (byte)2);
            Codec codec = codecFactory.create_codec(encoding);
            aServiceInterceptor.setCodec(codec);
            
            AServiceIORInterceptor aServiceIORInterceptor =
                new AServiceIORInterceptor(codec);
            info.add_ior_interceptor(aServiceIORInterceptor);

        } catch (Throwable t) {
            System.out.println("Exception handling not shown.");
        }
    }

}
 
// End of file.
