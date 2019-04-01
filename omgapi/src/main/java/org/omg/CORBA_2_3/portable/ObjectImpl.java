/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA_2_3.portable;

import org.omg.CORBA_2_3.portable.Delegate;

/**
 * ObjectImpl class is the base class for all stubs. It provides the basic delegation mechanism. It extends
 * org.omg.CORBA.portable.ObjectImpl and provides new methods defined by CORBA 2.3.
 *
 * @see org.omg.CORBA.portable.ObjectImpl
 * @author OMG
 * @version 1.16 07/27/07
 * @since JDK1.2
 */

public abstract class ObjectImpl extends org.omg.CORBA.portable.ObjectImpl {

    /**
     * Returns the codebase for this object reference.
     *
     * @return the codebase as a space delimited list of url strings or null if none.
     */
    public java.lang.String _get_codebase() {
        org.omg.CORBA.portable.Delegate delegate = _get_delegate();
        if (delegate instanceof Delegate)
            return ((Delegate) delegate).get_codebase(this);
        return null;
    }
}
