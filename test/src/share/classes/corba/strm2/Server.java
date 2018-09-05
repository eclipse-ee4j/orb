/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.strm2;

import java.rmi.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.*;
import java.util.*;

public class Server
{
    private static InitialContext rootContext ;

    public static void main(String[] args) {
        try {

            rootContext = new InitialContext();

            TesterImpl testerImpl = new TesterImpl();
            rootContext.rebind(testerImpl.getDescription(),
                               testerImpl);

            System.out.println("Server is ready.");

            // Stay alive so nothing strange happens to
            // the System streams
            Object synchObj = new Object();
            synchronized(synchObj) {
                synchObj.wait();
            }

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}

