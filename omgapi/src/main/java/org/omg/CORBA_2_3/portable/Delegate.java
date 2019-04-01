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

/**
 * Delegate class provides the ORB vendor specific implementation of CORBA object. It extends
 * org.omg.CORBA.portable.Delegate and provides new methods that were defined by CORBA 2.3.
 *
 * @see org.omg.CORBA.portable.Delegate
 * @author OMG
 * @version 1.16 07/27/07
 * @since JDK1.2
 */

public abstract class Delegate extends org.omg.CORBA.portable.Delegate {

    /**
     * Returns the codebase for object reference provided.
     *
     * @param self the object reference whose codebase needs to be returned.
     * @return the codebase as a space delimited list of url strings or null if none.
     */
    public java.lang.String get_codebase(org.omg.CORBA.Object self) {
        return null;
    }
}
