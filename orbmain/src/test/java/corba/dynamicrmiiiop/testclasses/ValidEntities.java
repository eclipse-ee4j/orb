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
 * Valid IDL Entity Types
 */
public class ValidEntities {

    public static final Class[] CLASSES = {
        ValidEntity1.class, ValidEntity2.class
    };

    public class ValidEntity1 implements org.omg.CORBA.portable.IDLEntity {}

    public class ValidEntity2 extends ValidEntity1 {}

}
