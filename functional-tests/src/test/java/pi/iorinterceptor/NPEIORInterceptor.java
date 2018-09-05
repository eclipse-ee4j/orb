/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.iorinterceptor;

import java.io.*;

import org.omg.CORBA.LocalObject;

import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;

/**
 * An IORInterceptor that throws a NullPointerException during its
 * establish_components.
 */
public class NPEIORInterceptor 
    extends LocalObject 
    implements IORInterceptor 
{

    // The name for this interceptor
    private String name;

    // Destination for all output, set from constructor upon construction
    // by ServerTestInitializer:
    private PrintStream out;

    // True if any instances of NPEIORInterceptor were ever registered, or
    // false if not.
    public static boolean registered = false;

    // True if establish_components was ever called on this interceptor,
    // or false if not
    public static boolean establishComponentsCalled = false;

    public NPEIORInterceptor( String name, PrintStream out ) {
        this.name = name;
        this.out = out;
        out.println( "    - NPEIORInterceptor " + name + " created." );
        registered = true;
    }

    public String name() {
        return name;
    }

    public void destroy() {
    }

    public void establish_components (IORInfo info) {
        out.println( "    - NPEIORInterceptor: establish_components called." );
        establishComponentsCalled = true;
        throw new NullPointerException();
    }

    public void components_established( IORInfo info )
    {
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates, 
        short state )
    {
    }

    public void adapter_manager_state_changed( int managerId, short state )
    {
    }
}


