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


class idlI1Servant
    extends
        idlI1POA
{
    public idlI1Servant()
    {
    }

    public String o1(String arg)
    {
        System.out.println( "idlI1.o1 with " + arg );
        System.out.flush( );
        return "return value for o1";
    }

    public String o2(String arg)
    {
        System.out.println( "idlI1.o2 with " + arg );
        System.out.flush( );
        return "return value for o2";
    }
}

