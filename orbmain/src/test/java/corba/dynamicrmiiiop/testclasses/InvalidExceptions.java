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
 * Invalid RMI/IDL Exception Types
 */
public class InvalidExceptions {

    public static final Class[] CLASSES = {
        InvalidException1.class, 
        InvalidException2.class,
        InvalidException3.class,
        InvalidException4.class,
        InvalidException5.class, 
        InvalidException6.class,
        InvalidException7.class,
        InvalidException8.class,
        InvalidException9.class, 
        InvalidException10.class
    };

    // must be a checked exception
    public class InvalidException1 {}

    // must be a checked exception
    public class InvalidException2 extends InvalidException1 {}

    // must be a checked exception
    public class InvalidException3 extends Error {}

    // must be a checked exception
    public class InvalidException4 extends InvalidException3 {}

    // must be a checked exception
    public class InvalidException5 extends RuntimeException {}

    // must be a checked exception
    public class InvalidException6 extends InvalidException5 {}

    // must be a checked exception
    public interface InvalidException7 {}

    // must be a checked exception
    public interface InvalidException8 extends java.io.Serializable {}

    public class InvalidException9 extends Exception 
        implements java.rmi.Remote {}

    public class InvalidException10 extends InvalidException9 {}

    
}
