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
// Created       : 2004 Jun 13 (Sun) 13:49:39 by Harold Carr.
// Last Modified : 2005 Jun 01 (Wed) 11:07:47 by Harold Carr.
//

package corba.folb_8_1;

public class ZeroPortServer2
{
    public static void main(String[] av)
    {
        ZeroPortServer1.serverName = Common.zero2;
        ZeroPortServer1.socketPorts = Common.zero2Ports;
        ZeroPortServer1.main(av);
    }
}

// End of file.
