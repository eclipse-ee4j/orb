/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA;

import org.omg.CORBA.portable.ObjectImpl;

/**
 * @deprecated org.omg.CORBA.DynamicImplementation
 */

public class DynamicImplementation extends org.omg.CORBA.portable.ObjectImpl {

    /**
      * @deprecated Deprecated by Portable Object Adapter
      */

    public void invoke(ServerRequest request) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public String[] _ids() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
