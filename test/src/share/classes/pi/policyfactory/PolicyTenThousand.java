/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.policyfactory;

import java.util.*;
import java.io.*;
import org.omg.CORBA.*;

/** This Policy object's type is 10000.
 */
public class PolicyTenThousand extends LocalObject
    implements Policy
{
    public int policy_type( ) {
        return 10000;
    }


    public org.omg.CORBA.Policy copy( ) {
        return this;
    }

    public void destroy ( ) {
        // Do Nothing
    }
}
  
