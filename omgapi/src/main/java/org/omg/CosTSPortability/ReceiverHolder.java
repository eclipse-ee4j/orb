/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//      org/omg/CosTSPortability/ReceiverHolder.java

package org.omg.CosTSPortability;

public class ReceiverHolder {
    // instance variable
    public org.omg.CosTSPortability.Receiver value;

    // constructors
    public ReceiverHolder() {
        this(null);
    }

    public ReceiverHolder(org.omg.CosTSPortability.Receiver __arg) {
        value = __arg;
    }
}
