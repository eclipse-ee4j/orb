/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
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
