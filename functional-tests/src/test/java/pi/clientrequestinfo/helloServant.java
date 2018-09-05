/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
