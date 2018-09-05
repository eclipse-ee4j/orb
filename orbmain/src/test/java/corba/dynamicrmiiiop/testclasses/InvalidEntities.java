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
 * Invalid IDL Entity Types
 */
public class InvalidEntities {

    public static final Class[] CLASSES = {
        InvalidEntity1.class, 
        InvalidEntity2.class,
        InvalidEntity3.class, 
        InvalidEntity4.class,
        InvalidEntity5.class, 
        InvalidEntity6.class
    };

    // must be a class that is a subtype of org.omg.CORBA.portable.IDLEntity
    public class InvalidEntity1 {}
    
    // must be a class that is a subtype of org.omg.CORBA.portable.IDLEntity
    public class InvalidEntity2 extends InvalidEntity1 {}

    // must be a class 
    public interface InvalidEntity3 {}
    
    public interface InvalidEntity4 extends InvalidEntity3 {}

    public interface InvalidEntity5 extends org.omg.CORBA.portable.IDLEntity {}

    public interface InvalidEntity6 extends InvalidEntity5 {}

}
