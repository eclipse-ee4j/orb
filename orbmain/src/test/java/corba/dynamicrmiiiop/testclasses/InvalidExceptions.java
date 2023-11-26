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
 * Invalid RMI/IDL Exception Types
 */
public class InvalidExceptions {

    public static final Class[] CLASSES = { InvalidException1.class, InvalidException2.class, InvalidException3.class,
            InvalidException4.class, InvalidException5.class, InvalidException6.class, InvalidException7.class, InvalidException8.class,
            InvalidException9.class, InvalidException10.class };

    // must be a checked exception
    public class InvalidException1 {
    }

    // must be a checked exception
    public class InvalidException2 extends InvalidException1 {
    }

    // must be a checked exception
    public class InvalidException3 extends Error {
    }

    // must be a checked exception
    public class InvalidException4 extends InvalidException3 {
    }

    // must be a checked exception
    public class InvalidException5 extends RuntimeException {
    }

    // must be a checked exception
    public class InvalidException6 extends InvalidException5 {
    }

    // must be a checked exception
    public interface InvalidException7 {
    }

    // must be a checked exception
    public interface InvalidException8 extends java.io.Serializable {
    }

    public class InvalidException9 extends Exception implements java.rmi.Remote {
    }

    public class InvalidException10 extends InvalidException9 {
    }

}
