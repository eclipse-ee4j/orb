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
 * Valid RMI/IDL CORBA Object Reference Types
 */
public class ValidObjRefs {

    public static final Class[] CLASSES = {
        org.omg.CORBA.Object.class,
        ValidObjRef1.class, ValidObjRef2.class
    };

    public interface ValidObjRef1 extends org.omg.CORBA.Object {}

    public interface ValidObjRef2 extends ValidObjRef1 {}

}
