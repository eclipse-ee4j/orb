/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.dynamicrmiiiop.testclasses;

/**
 * Valid RMI/IDL Value Types
 */
public class ValidValues {

    public static final Class[] CLASSES = {
        ValidValue1.class, ValidValue2.class,
        java.util.Date.class, java.lang.Integer.class, java.lang.String.class
    };

    public class ValidValue1 implements java.io.Serializable {}

    public class ValidValue2 extends ValidValue1 {}


}
