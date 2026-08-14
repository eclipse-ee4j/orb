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

    public helloDelegate(PrintStream out) {
        super();
        this.out = out;
    }

    public String sayHello() {
        out.println("helloServant: sayHello() invoked");
        invoked = true;
        return "Hello, world!";
    }

    public String saySystemException() {
        out.println("helloServant: saySystemException() invoked");
        invoked = true;
        throw new UNKNOWN("Valid Test Result");
    }

    // This will cause a receive_reply to be invoked since this
    // is a one-way method.
    public void sayOneway() {
        out.println("helloServant: sayOneway() invoked");
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
