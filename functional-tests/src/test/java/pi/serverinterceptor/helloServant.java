/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.serverinterceptor;

import org.omg.CORBA.*;

import java.util.*;
import java.io.*;
import org.omg.PortableInterceptor.*;

import ServerRequestInterceptor.*;

/**
 * Servant implementation.  
 */
class helloServant extends helloPOA {
    // The object to delegate all calls to:
    helloDelegate delegate;

    public helloServant( PrintStream out, String symbol ) {
        super();
        this.delegate = new helloDelegate( out, symbol );
    }

    public String sayHello() {
        return delegate.sayHello();
    }

    public void sayOneway() {
        delegate.sayOneway();
    }
    
    public void saySystemException() {
        delegate.saySystemException();
    }

    public void sayUserException() 
        throws ForwardRequest
    {
        delegate.sayUserException();
    }
    
    public String syncWithServer( boolean exceptionRaised ) {
        return delegate.syncWithServer( exceptionRaised );
    }

}
