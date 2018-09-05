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
 * Invalid RMI/IDL CORBA Object Reference Types
 */
public class InvalidObjRefs {

    public static final Class[] CLASSES = {
        InvalidObjRef1.class, 
        InvalidObjRef2.class,
        InvalidObjRef3.class, 
        InvalidObjRef4.class,
        InvalidObjRef5.class, 
        InvalidObjRef6.class,
        InvalidObjRef7.class, 
        InvalidObjRef8.class
    };

    // must be subtype of org.omg.CORBA.Object
    public interface InvalidObjRef1 {}

    // must be subtype of org.omg.CORBA.Object
    public interface InvalidObjRef2 extends InvalidObjRef1 {}

    // must be subtype of org.omg.CORBA.Object
    public class InvalidObjRef3 {}

    // must be subtype of org.omg.CORBA.Object
    public class InvalidObjRef4 extends InvalidObjRef3 {}

    // must be an interface
    public abstract class InvalidObjRef5 
        extends org.omg.CORBA.portable.ObjectImpl {}

    // must be an interface
    public abstract class InvalidObjRef6 extends InvalidObjRef5 {}

    // must be an interface
    public abstract class InvalidObjRef7 implements org.omg.CORBA.Object {}

    // must be an interface
    public abstract class InvalidObjRef8 extends InvalidObjRef7 {}

}
