/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.clientinterceptor;

import org.omg.CORBA.*;

import java.io.*;
import java.rmi.*;
import javax.rmi.*;
import javax.naming.*;

import ClientRequestInterceptor.*; // hello interface

/**
 * ClientDelegate implementation with old stubs.   
 */
public class helloOldRMIIIOP
    extends _helloImplBase
{
    // Contains the actual implementation of the hello interface.
    private helloDelegate delegate = null;
    
    public helloOldRMIIIOP( PrintStream out ) 
        throws RemoteException 
    {
        super();
        this.delegate = new helloDelegate( out );
    }

    public helloDelegate getDelegate() {
        return delegate;
    }

    public String sayHello() {
        return delegate.sayHello();
    }
    
    public String saySystemException() {
        return delegate.saySystemException();
    }
    
    public void sayOneway() {
        delegate.sayOneway();
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
