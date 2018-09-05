/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.ort;

import java.io.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.*;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TAG_INTERNET_IOP;

/**
 * Thoroughly tests IORInterceptor support.
 */
public class SampleIORInterceptor 
    extends LocalObject 
    implements IORInterceptor_3_0 
{

    // The name for this interceptor
    private String name;

    // Destination for all output.  This is set in the constructor, which
    // is called by ServerTestInitializer.
    private PrintStream out;

    public SampleIORInterceptor( String name, PrintStream out ) {
        this.name = name;
        this.out = out;
        out.println( "    - IORInterceptor " + name + " created." );
    }

    public String name() {
        return name;
    }

    public void destroy() {
    }

    public void establish_components (IORInfo info) {
    }

    /**
     * Check ORBId and ORBServerId are propogated correctly.
     */
    public void components_established( IORInfo info )
    {
        com.sun.corba.ee.impl.interceptors.IORInfoImpl iorInfoImpl =
            (com.sun.corba.ee.impl.interceptors.IORInfoImpl) info;
        ObjectReferenceTemplate ort = iorInfoImpl.adapter_template();
        if( !ort.orb_id().equals( Constants.ORB_ID )  && 
            !ort.orb_id().equals(com.sun.corba.ee.impl.ior.ObjectKeyTemplateBase.JIDL_ORB_ID)) {
            System.err.println( 
                "ORBId is not passed to components_established correctly..");
            System.exit( -1 );
        }

        if( !ort.server_id().equals( Constants.ORB_SERVER_ID ) ) {
            System.err.println( 
                "ORBServerId is not passed to components_established correctly..");
            System.exit( -1 );
        }
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates, 
        short state )
    {
        ORTStateChangeEvaluator.getInstance( ).registerAdapterStateChange( 
            templates, state );
    }

    public void adapter_manager_state_changed( int managedId, short state )
    {
        ORTStateChangeEvaluator.getInstance().registerAdapterManagerStateChange(
            managedId, state );
    }
}


