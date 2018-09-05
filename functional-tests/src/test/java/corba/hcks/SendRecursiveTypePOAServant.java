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
// Created       : 2002 Jul 12 (Fri) 09:42:02 by Harold Carr.
// Last Modified : 2002 Jul 12 (Fri) 09:54:51 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import java.util.Properties;

class SendRecursiveTypePOAServant
    extends 
        sendRecursiveTypePOA
{
    public static final String baseMsg = SendRecursiveTypePOAServant.class.getName();

    public ORB orb;

    public SendRecursiveTypePOAServant(ORB orb)
    {
        this.orb = orb;
    }

    public Any sendAsAny (Any x)
    {
        return x;
    }

    public recursiveType sendAsType(recursiveType x)
    {
        return x;
    }
}

// End of file.

