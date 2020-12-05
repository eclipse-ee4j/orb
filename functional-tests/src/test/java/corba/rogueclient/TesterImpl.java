/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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

    @Override
    public String passString(String theString) throws RemoteException {
        // reverse the string and send it back
        StringBuilder sb = new StringBuilder(theString.length());
        for (int i = (theString.length() - 1); i >= 0; i--) {
            sb.append(theString.charAt(i));
        }

        String result = sb.toString();
        return result;
    }
}

