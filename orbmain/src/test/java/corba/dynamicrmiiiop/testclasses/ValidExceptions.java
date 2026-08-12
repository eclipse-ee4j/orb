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
 * Valid RMI/IDL Exception Types
 */
public class ValidExceptions {

    public static final Class[] CLASSES = { ValidException1.class, ValidException2.class, ValidException3.class, ValidException4.class,
            ValidException5.class, ValidException6.class };

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
