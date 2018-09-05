/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poapolicies;

import HelloStuff.HelloPOA;

public class HelloImpl extends HelloPOA
{
    public byte[] id;
    public HelloImpl() {
        id = null;
    }
    public HelloImpl(byte[] oid) {
        id = oid;
    }
    public String hi() {
        if (id == null)
            return "Welcome, POA";
        else
            return "Welcome, POA (oid = "+new String(id)+")";
    }
}
        

