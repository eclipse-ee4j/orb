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
// Created       : 2003 Apr 09 (Wed) 16:28:12 by Harold Carr.
// Last Modified : 2003 Sep 27 (Sat) 21:11:16 by Harold Carr.
//

package corba.connections;

public class Server1
{
    public static void main(String[] av)
    {    
        String[] args = { Server.server1, Server.service11, Server.service12 };
        Server.main(args);
    }
}

// End of file.

