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
import javax.rmi.*;
import javax.naming.*;
import java.util.*;

import ClientRequestInterceptor.*; // hello interface

/**
 * Contains the actual implementations of hello operations.
 */
public class helloDelegate extends Observable implements helloIF {
    private boolean invoked = false;
    private PrintStream out = null;
    
    public helloDelegate( PrintStream out ) {
        super();
        this.out = out;
    }

    public String sayHello() {
        out.println( "helloServant: sayHello() invoked" );
        invoked = true;
        return "Hello, world!";
    }
    
    public String saySystemException() {
        out.println( "helloServant: saySystemException() invoked" );
        invoked = true;
        throw new UNKNOWN( "Valid Test Result" );
    }
    
    // This will cause a receive_reply to be invoked since this
    // is a one-way method.
    public void sayOneway() {
        out.println( "helloServant: sayOneway() invoked" );
        invoked = true;
    }
    
    public void clearInvoked() {
        invoked = false;
    }
    
    public boolean wasInvoked() {
        return invoked;
    }

    public void resetServant() {
        setChanged();
        notifyObservers();
    }
}
