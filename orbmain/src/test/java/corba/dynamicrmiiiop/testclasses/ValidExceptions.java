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
 * Valid RMI/IDL Exception Types
 */
public class ValidExceptions {

    public static final Class[] CLASSES = { ValidException1.class, ValidException2.class, ValidException3.class, ValidException4.class, ValidException5.class,
            ValidException6.class };

    public class ValidException1 extends java.lang.Exception {
    }

    public class ValidException2 extends ValidException1 {
    }

    public class ValidException3 extends Throwable {
    }

    public class ValidException4 extends ValidException3 {
    }

    public class ValidException5 extends java.io.IOException {
    }

    public class ValidException6 extends ValidException5 {
    }

}
