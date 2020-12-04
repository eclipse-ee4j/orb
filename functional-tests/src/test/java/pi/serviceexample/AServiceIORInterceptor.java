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

//
// Created       : 2001 Sep 24 (Mon) 20:51:03 by Harold Carr.
// Last Modified : 2001 Oct 02 (Tue) 20:49:16 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;

public class AServiceIORInterceptor
    extends org.omg.CORBA.LocalObject
    implements IORInterceptor
{
    private Codec codec;

    public AServiceIORInterceptor(Codec codec)
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
    // IOR Interceptor operations
    //

    public void establish_components(IORInfo info)
    {
        //
        // Note: typically, rather than just inserting a tagged component
        // this interceptor would check info.get_effective_policy(int)
        // to determine if a tagged component reflecting that policy
        // should be added to the IOR.  That is not shown in this example.
        // 

        ASERVICE_COMPONENT aServiceComponent = new ASERVICE_COMPONENT(true);
        Any any = ORB.init().create_any();
        ASERVICE_COMPONENTHelper.insert(any, aServiceComponent);
        byte[] value = null;
        try {
            value = codec.encode_value(any);
        } catch (InvalidTypeForEncoding e) {
            System.out.println("Exception handling not shown.");
        }
        TaggedComponent taggedComponent =
            new TaggedComponent(TAG_ASERVICE_COMPONENT.value, value);
        info.add_ior_component(taggedComponent);
    }

}

// End of file.

