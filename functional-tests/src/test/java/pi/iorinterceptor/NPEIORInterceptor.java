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


