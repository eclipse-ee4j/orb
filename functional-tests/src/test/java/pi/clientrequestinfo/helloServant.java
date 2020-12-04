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

package pi.clientrequestinfo;

import org.omg.CORBA.*;

import java.util.*;
import java.io.*;
import org.omg.CORBA.*;

import ClientRequestInfo.*; // hello interface

/**
 * Servant implementation.  
 */
public class helloServant 
        extends helloPOA 
{
    // contains implementations of methods.
    private helloDelegate delegate;

    public helloServant( PrintStream out ) {
        super();
        delegate = new helloDelegate( out );
    }

    public String sayHello() {
        return delegate.sayHello();
    }
    
    public String saySystemException() {
        return delegate.saySystemException();
    }
    
    public void sayUserException() 
        throws ExampleException 
    {
        delegate.sayUserException();
    }
    
    public void sayOneway() {
        delegate.sayOneway();
    }

    public String sayArguments( String arg1, int arg2, boolean arg3 ) {
        return delegate.sayArguments( arg1, arg2, arg3 );
    }

    public void clearInvoked() {
        delegate.clearInvoked();
    }
    
    public boolean wasInvoked() {
        return delegate.wasInvoked();
    }

    public void resetServant() {
        delegate.resetServant();
    }
}
