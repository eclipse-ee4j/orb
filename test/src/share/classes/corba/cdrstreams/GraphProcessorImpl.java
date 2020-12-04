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

package corba.cdrstreams;

import java.rmi.RemoteException ;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB ;
import java.io.File ;
import java.io.RandomAccessFile ;
import org.omg.PortableServer.POA ;
import java.util.*;

public class GraphProcessorImpl extends PortableRemoteObject implements GraphProcessor
{
    public GraphProcessorImpl() throws RemoteException
    {
        super();
    }

    public void process(Node graphStart) throws InvalidGraphException
    {
        Vector links = graphStart.links;

        if (links == null)
            throw new InvalidGraphException("Links vector cannot be null");

        System.out.println("Received: " + graphStart.value);

        System.out.println("Value size: " + graphStart.value.length());
        System.out.println("Number of links: " + links.size());

        Enumeration enumeration = links.elements();

        int i = 0;
        while (enumeration.hasMoreElements()) {
            Node node = (Node)enumeration.nextElement();
            System.out.println("Link " + (i++) + ": "
                               + (node == graphStart ? "good" : "bad"));
            
        }
    }

    public Object verifyTransmission(Object input) 
    {
        return input;
    }

    public boolean receiveObject(Object input)
    {
        return input != null;
    }
}
