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
 * Java class whose name clashes with an IDL keyword.
 */
public class Raises {

    // Inner class with name that clashes with an IDL keyword.
    public static class Union {
    }

    // Inner class whose name has a leading underscore.
    public static class _Foo {
    }

    // Inner class with chars that need to be mangled.
    // public static class Uni\u8001code {}

    // Inner class with name that has leading underscore plus IDL keyword
    public static class _Union {
    }

}
