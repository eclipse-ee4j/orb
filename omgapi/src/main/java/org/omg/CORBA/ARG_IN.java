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

/**
 * Signifies an "input" argument to an invocation, meaning that the argument is being passed from the client to the
 * server. <code>ARG_IN.value</code> is one of the possible values used to indicate the direction in which a parameter
 * is being passed during an invocation performed using the Dynamic Invocation Interface (DII).
 * <P>
 * The code fragment below shows a typical usage:
 *
 * <PRE>
 * ORB orb = ORB.init(args, null);
 * org.omg.CORBA.NamedValue nv = orb.create_named_value("IDLArgumentIdentifier", myAny, org.omg.CORBA.ARG_IN.value);
 * </PRE>
 *
 * @version 1.5, 09/09/97
 * @see org.omg.CORBA.NamedValue
 * @since JDK1.2
 */
public interface ARG_IN {

    /**
     * The value indicating an input argument.
     */
    int value = 1;
}
