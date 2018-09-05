/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.serialization.proxy;

import java.rmi.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.*;

public class Server {

    public static void main(final String[] args) throws Exception {
        FrobnicatorProvider obj;
        try {
            System.setSecurityManager(new NoSecurityManager());
            obj = new FrobnicatorProviderBean();
            Context initialNamingContext = new InitialContext();
            initialNamingContext.rebind("DynamicProxyBug1368", obj);
            // below print line is important, else the testframe does not work
            System.out.println("Server is ready.");
            System.out.flush();
            java.lang.Object sync = new java.lang.Object();
            synchronized (sync) { sync.wait(); }
        } catch (final Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}

