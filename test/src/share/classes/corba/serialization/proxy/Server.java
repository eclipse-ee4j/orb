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

