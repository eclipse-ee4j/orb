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
// Created       : 2001 May 23 (Wed) 19:46:30 by Harold Carr.
// Last Modified : 2001 Oct 02 (Tue) 20:41:23 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;

public class AServiceInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor, ServerRequestInterceptor
{
    private int slotId;
    private Codec codec;

    private static final int serviceContextId = 1234;

    public AServiceInterceptor(int slotId)
    {
        this.slotId = slotId;
    }

    void setCodec(Codec codec)
    {
        this.codec = codec;
    }

    //
    // Interceptor operations
    //

    public String name() 
    {
        return "AServiceInterceptor";
    }

    public void destroy() 
    {
    }

    //
    // ClientRequestInterceptor operations
    //

    public void send_request(ClientRequestInfo ri)
    {
        //
        // See if the target object contains an ASERVICE_COMPONENT.
        //

        try {
            TaggedComponent taggedComponent =
                ri.get_effective_component(TAG_ASERVICE_COMPONENT.value);

            Any sAny = null;
            try {
                sAny = codec.decode_value(taggedComponent.component_data,
                                          ASERVICE_COMPONENTHelper.type());
            } catch (TypeMismatch e) {
                System.out.println("Exception handling not shown.");
            } catch (FormatMismatch e) {
                System.out.println("Exception handling not shown.");
            }

            ASERVICE_COMPONENT aServiceComponent =
                ASERVICE_COMPONENTHelper.extract(sAny);

            //
            // Only send the service context if the target object requires it.
            //

            if (aServiceComponent.requiresAService) {
                try {
                    Any any = ri.get_slot(slotId);
                    if (any.type().kind().equals(TCKind.tk_long)) {
                        int serviceId = any.extract_long();
                        byte[] serviceContextData = {
                            // Little endian to make it
                            // easier to see in debugger.
                            (byte)((serviceId >>>  0) &  0xFF),
                            (byte)((serviceId >>>  8) &  0xFF),
                            (byte)((serviceId >>> 16) &  0xFF),
                            (byte)((serviceId >>> 24) &  0xFF)
                        };
                        ri.add_request_service_context(
                            new ServiceContext(serviceContextId,
                                               serviceContextData),
                            false);
                    }
                } catch (InvalidSlot e) {
                    System.out.println("Exception handling not shown.");
                }
            }
        } catch (BAD_PARAM e) {
            // If it is not present, do nothing.
            ;
        }
    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
    }

    public void receive_exception(ClientRequestInfo ri)
    {
    }

    public void receive_other(ClientRequestInfo ri)
    {
    }

    //
    // ServerRequestInterceptor operations
    //

    public void receive_request_service_contexts(ServerRequestInfo ri)
    {
        try {
            ServiceContext serviceContext =
                ri.get_request_service_context(serviceContextId);
            byte[] data = serviceContext.context_data;
            int b1, b2, b3, b4;
            b4 = (data[0] <<  0) & 0x000000FF;
            b3 = (data[1] <<  8) & 0x0000FF00;
            b2 = (data[2] << 16) & 0x00FF0000;
            b1 = (data[3] << 24) & 0xFF000000;
            int serviceId = (b1 | b2 | b3 | b4);
            Any any = ORB.init().create_any();
            any.insert_long(serviceId);
            ri.set_slot(slotId, any);
        } catch (BAD_PARAM e) {
            // Not present means service is not in effect.
            // Do nothing.
            ;
        } catch (InvalidSlot e) {
            System.out.println("Exception handling not shown.");
        }
    }

    public void receive_request(ServerRequestInfo ri)
    {
    }

    public void send_reply(ServerRequestInfo ri)
    {
    }

    public void send_exception(ServerRequestInfo ri)
    {
    }

    public void send_other(ServerRequestInfo ri)
    {
    }
}

// End of file.

