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

/** This is the Policy Factory to create PolicyHundred object.
 */
public class PolicyFactoryHundred extends LocalObject
    implements org.omg.PortableInterceptor.PolicyFactory
{
    public Policy  create_policy( int type, Any val ) {
        System.out.println( "PolicyFactoryHundred.create_policy called..." );
        System.out.flush();
        return new PolicyHundred();
    }
}
  
