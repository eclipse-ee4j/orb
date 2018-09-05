/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//      org/omg/CosTSPortability/SenderHolder.java

package org.omg.CosTSPortability;
public class SenderHolder
{
    //  instance variable 
    public org.omg.CosTSPortability.Sender value;
    //  constructors 
    public SenderHolder() {
        this(null);
    }
    public SenderHolder(org.omg.CosTSPortability.Sender __arg) {
        value = __arg;
    }
}
