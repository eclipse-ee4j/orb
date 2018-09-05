/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.rogueclient;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;

public class TesterImpl extends PortableRemoteObject implements Tester
{
    public TesterImpl() throws RemoteException {
        super();
    }

    public String getDescription() throws RemoteException {
        return "Tester supports a remote method, String passString(String)";
    }

    public String passString(String theString) throws RemoteException {
        // reverse the string and send it back
        StringBuffer sb = new StringBuffer(theString.length());
        for (int i = (theString.length() - 1); i >= 0; i--) {
            sb.append(theString.charAt(i));
        }

        String result = sb.toString();
        return result;
    }
}

