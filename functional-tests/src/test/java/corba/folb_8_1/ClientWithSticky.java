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
// Last Modified : 2005 Jun 01 (Wed) 11:05:26 by Harold Carr.
//

package corba.folb_8_1;

public class ClientWithSticky
{
    public static void main(String[] av)
    {
        Client.withSticky = true;
        Client.main(av);
    }
}

// End of file.
