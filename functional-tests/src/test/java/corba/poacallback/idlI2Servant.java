/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poacallback;


class idlI2Servant
    extends
        idlI2POA
{
    public idlI2Servant()
    {
    }

    public String o(String arg1)
    {
        System.out.println( "idlI2 Servant called with " + arg1 );
        System.out.flush( );
        return "return value for o";
    }
}

