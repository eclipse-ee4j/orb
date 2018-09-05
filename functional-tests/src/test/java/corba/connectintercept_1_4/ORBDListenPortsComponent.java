/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2000 Oct 17 (Tue) 08:36:51 by Harold Carr.
// Last Modified : 2002 Dec 04 (Wed) 21:00:53 by Harold Carr.
//

package corba.connectintercept_1_4;

import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.TaggedComponentBase;

public class ORBDListenPortsComponent
    extends
        TaggedComponentBase
{
    private String listenPorts;

    public ORBDListenPortsComponent(String listenPorts)
    {
        this.listenPorts = listenPorts;
    }

    public boolean equals(Object o)
    {
        if (o == null) {
            return false;
        }
        if (!(o instanceof ORBDListenPortsComponent)) {
            return false;
        }
        return listenPorts.equals(((ORBDListenPortsComponent)o).listenPorts);
    }

    public void writeContents(OutputStream os)
    {
        os.write_string(listenPorts);
    }

    public int getId()
    {
        return Common.ListenPortsComponentID;
    }
}

// End of file.
